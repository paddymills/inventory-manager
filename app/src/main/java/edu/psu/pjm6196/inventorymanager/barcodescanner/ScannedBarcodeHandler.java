package edu.psu.pjm6196.inventorymanager.barcodescanner;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.List;

import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.BarcodeGraphic;

public interface ScannedBarcodeHandler {
    void imageAnalyzedWithBarcodes(List<Barcode> barcodes, @NonNull Bitmap image);

    void updateTelemetry(long frameLatency, long detectorLatency, int framesPerSecond);

    List<BarcodeGraphic> getBarcodesToRender();

    GraphicOverlay getOverlay();
}
