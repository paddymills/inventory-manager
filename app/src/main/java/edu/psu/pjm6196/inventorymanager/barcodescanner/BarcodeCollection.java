package edu.psu.pjm6196.inventorymanager.barcodescanner;

import android.util.Log;
import android.view.MotionEvent;

import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.BarcodeGraphic;

public class BarcodeCollection {
    private static final String TAG = "BarcodeCollection";
    private final ConcurrentHashMap<String, BarcodeGraphic> barcodes;

    public BarcodeCollection() {
        barcodes = new ConcurrentHashMap<>();
    }

    public ArrayList<String> handleTouchEvent(MotionEvent touch) {
        ArrayList<String> touchedBarcodes = new ArrayList<>();

        float x = touch.getX();
        float y = touch.getY();
        for (Map.Entry<String, BarcodeGraphic> entry : barcodes.entrySet()) {
            if (entry.getValue().isTouchInRect(x, y)) {
                Log.d(TAG, String.format("Barcode %s selected", entry.getKey()));

                touchedBarcodes.add(entry.getKey());
            }
        }

        return touchedBarcodes;
    }

    public void clearSelected() {
        barcodes.values().forEach(BarcodeGraphic::clearSelected);
    }

    public void commitBarcodeTouchEvents(List<String> touchedBarcodes) {
        barcodes.entrySet().stream()
            .filter(x -> touchedBarcodes.contains(x.getKey()))
            .forEach(x -> x.getValue().toggleSelected());
    }

    public int getToBeNumberOfBarcodesSelected(List<String> touchedBarcodes) {
        return (int) barcodes.entrySet().stream()
            // barcode will end up selected (after commit) only if one of:
            //    - it is already selected
            //    - it was just selected
            .filter(x -> Boolean.logicalXor(
                x.getValue().isSelected(),
                touchedBarcodes.contains(x.getKey())
            ))
            .count();
    }

    private Stream<BarcodeGraphic> getSelectedBarcodes() {
        return barcodes.values().stream().filter(BarcodeGraphic::isSelected);
    }

    public List<String> getScannedBarcodeIds() {
        return barcodes.values().stream().map(BarcodeGraphic::toString).collect(Collectors.toList());
    }

    public List<BarcodeGraphic> getBarcodesInView() {
        return new ArrayList<>(barcodes.values());
    }

    public int getNumberOfBarcodesSelected() {
        return (int) getSelectedBarcodes().count();
    }

    public List<String> getSelectedBarcodeIds() {
        return getSelectedBarcodes().map(BarcodeGraphic::toString).collect(Collectors.toList());
    }

    public String getSelectedBarcodeId() {
        return getSelectedBarcodeIds().get(0);
    }

    public void addBarcode(Barcode barcode) {
        String key = barcode.getDisplayValue();
        BarcodeGraphic sb = new BarcodeGraphic(barcode, false);

        assert key != null;
        BarcodeGraphic currentVal = this.barcodes.putIfAbsent(key, sb);
        if (currentVal != null) {
            // barcode exists -> retain existing attributes
            currentVal.setBarcode(barcode);
            sb = currentVal;

            this.barcodes.put(key, sb);
        }
    }
}
