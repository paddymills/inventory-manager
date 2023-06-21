package edu.psu.pjm6196.inventorymanager.barcodescanner;

import android.graphics.Canvas;

import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.ArrayList;

import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.BarcodeGraphic;
import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.CameraImageGraphic;
import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.TelemetryInfoGraphic;

public class GraphicOverlayFrame {
    private final ArrayList<Barcode> barcodes;
    private final boolean showTelemetry = false;
    private final TelemetryInfoGraphic telemetryGraphic;
    private final CameraImageGraphic imageGraphic;

    public GraphicOverlayFrame(ArrayList<Barcode> barcodes, CameraImageGraphic capture, TelemetryInfoGraphic telemetry) {
        this.barcodes = barcodes;
        this.imageGraphic = capture;
        this.telemetryGraphic = telemetry;
    }

    public void draw(GraphicOverlay overlay, Canvas canvas) {
        imageGraphic.draw(canvas, overlay);

        if (showTelemetry)
            telemetryGraphic.draw(canvas, overlay);

        for (Barcode barcode : barcodes) {
            new BarcodeGraphic(barcode, false).draw(canvas, overlay);
        }
    }
}
