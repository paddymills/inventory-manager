/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.psu.pjm6196.inventorymanager.barcodescanner.processor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import edu.psu.pjm6196.inventorymanager.barcodescanner.ScannedBarcodeHandler;
import edu.psu.pjm6196.inventorymanager.utils.PreferenceUtils;

/**
 * Barcode Detector Demo.
 */
public class BarcodeScannerProcessor extends VisionProcessorBase<List<Barcode>> {
    private static final String TAG = "BarcodeProcessor";
    protected ScannedBarcodeHandler barcodeScannerListener;
    private BarcodeScanner barcodeScanner;

    public BarcodeScannerProcessor(Context context, ScannedBarcodeHandler listener) {
        super(context);

        barcodeScannerListener = listener;
        init(context);
    }

    private static void logBarcodeData(Barcode barcode) {
        if (barcode == null || barcode.getCornerPoints() == null) return;

        // new corner logging view
        ArrayList<Point> pts = new ArrayList<>(Arrays.asList(barcode.getCornerPoints()));
        pts.sort((a, b) -> {
            // sort by Y-values and then X-values

            // comparator returns
            //  -1 -> <
            //   0 -> ==
            //   1 -> >
            if (a.y == b.y)
                return a.x - b.x;

            return a.y - b.y;
        });

        String barcodeText = barcode.getDisplayValue();
        if (barcodeText == null) barcodeText = "";

        int max_x = (int) Math.log10(pts.stream().mapToInt(p -> p.x).max().getAsInt()) + 1;
        int max_y = (int) Math.log10(pts.stream().mapToInt(p -> p.y).max().getAsInt()) + 1;

        StringBuilder val = new StringBuilder();
        Consumer<Point> padded_pair = (p) -> {
            val.append(String.format("(%" + max_x + "d,%" + max_y + "d)", p.x, p.y));
        };

        String type = switch (barcode.getFormat()) {
            case Barcode.FORMAT_AZTEC -> "Aztec";
            case Barcode.FORMAT_CODABAR -> "Codabar";
            case Barcode.FORMAT_CODE_39 -> "Code 39";
            case Barcode.FORMAT_CODE_93 -> "Code 93";
            case Barcode.FORMAT_CODE_128 -> "Code 128";
            case Barcode.FORMAT_DATA_MATRIX -> "Data Matrix";
            case Barcode.FORMAT_EAN_8 -> "EAN-8";
            case Barcode.FORMAT_EAN_13 -> "EAN-13";
            case Barcode.FORMAT_ITF -> "ITF";
            case Barcode.FORMAT_PDF417 -> "PDF417";
            case Barcode.FORMAT_QR_CODE -> "QR Code";
            case Barcode.FORMAT_UPC_A -> "UPC-A";
            case Barcode.FORMAT_UPC_E -> "UPC-E";
            default -> "unknown (" + barcode.getFormat() + ")";
        };

        padded_pair.accept(pts.get(0));
        val.append("----");
        padded_pair.accept(pts.get(1));
        val.append(String.format("\n|%" + (barcodeText.length() + 4) + "s|", type));
        val.append(String.format("\n|%" + (barcodeText.length() + 4) + "s|", "-".repeat(barcodeText.length())));
        val.append(String.format("\n|%" + (barcodeText.length() + 4) + "s|\n", barcodeText));
        padded_pair.accept(pts.get(2));
        val.append("----");
        padded_pair.accept(pts.get(3));
        Log.v("NewBarcodeLog", val.toString());
    }

    private void init(Context context) {
        int[] supportedFormats = PreferenceUtils.getAcceptedBarcodeFormats(context);

        barcodeScanner = BarcodeScanning.getClient(
            // detection is much faster if we specify the format, but this is optional
            new BarcodeScannerOptions.Builder()
                // this is annoying, but the function signature is
                //      .setBarcodeFormats(int, ...int)
                // which means that we cannot just pass an int[]
                .setBarcodeFormats(supportedFormats[0], supportedFormats)
                .build()
        );
    }


    @Override
    public void stop() {
        super.stop();
        barcodeScanner.close();
    }

    @Override
    protected Task<List<Barcode>> detectInImage(InputImage image) {
        return barcodeScanner.process(image);
    }

    @Override
    protected void onSuccess(@NonNull List<Barcode> detectedBarcodes, @NonNull Bitmap image) {
        if (detectedBarcodes.isEmpty()) {
//            barcodeScannerListener.validateOverlayState();
            return;
        }

        detectedBarcodes.forEach(BarcodeScannerProcessor::logBarcodeData);

        barcodeScannerListener.imageAnalyzedWithBarcodes(detectedBarcodes, image);
    }

    @Override
    protected void onTelemetryUpdate(long frameLatency, long detectorLatency, int framesPerSecond) {
        barcodeScannerListener.updateTelemetry(frameLatency, detectorLatency, framesPerSecond);
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Barcode detection failed " + e);
    }
}
