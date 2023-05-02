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

package edu.psu.pjm6196.inventorymanager.barcodescanner;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.psu.pjm6196.inventorymanager.barcodescanner.utils.GraphicOverlay;

/** Barcode Detector Demo. */
public class BarcodeScannerProcessor extends VisionProcessorBase<List<Barcode>> {

  private static final String TAG = "BarcodeProcessor";
  private static final long LIFETIME_DURATION = 250;

  private final BarcodeScanner barcodeScanner;

  private HashMap<String, ScannedBarcode> barcodes;

  private static class ScannedBarcode {
    private Barcode barcode;
    private long lastScannedTime;

    public ScannedBarcode(Barcode barcode) {
      this.barcode = barcode;
      this.lastScannedTime = System.currentTimeMillis();
    }

    public boolean needsRemoved() {
      return this.lastScannedTime - System.currentTimeMillis() > LIFETIME_DURATION;
    }

    public String getKey() {
      return this.barcode.getRawValue();
    }
  }

  public BarcodeScannerProcessor(Context context) {
    super(context);
    barcodes = new HashMap<>();

    barcodeScanner = BarcodeScanning.getClient(
        // detection is much faster if we specify the format, but this is optional
        new BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_PDF417)
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
  protected void onSuccess(@NonNull List<Barcode> barcodes, @NonNull GraphicOverlay graphicOverlay) {
    if (barcodes.isEmpty()) {
      Log.v(MANUAL_TESTING_LOG, "No barcode has been detected");
      validateBarcodesDisplayed(graphicOverlay);
    }

    for (int i = 0; i < barcodes.size(); ++i) {
      Barcode barcode = barcodes.get(i);
      this.barcodes.put(barcode.getRawValue(), new ScannedBarcode(barcode));

      graphicOverlay.add(barcode.getRawValue(), new BarcodeGraphic(graphicOverlay, barcode));
      logExtrasForTesting(barcode);
    }
  }

  private void validateBarcodesDisplayed(@NonNull GraphicOverlay graphicOverlay) {
    for ( Map.Entry<String, ScannedBarcode> barcode : this.barcodes.entrySet() ) {
      if ( barcode.getValue().needsRemoved() ) {
        graphicOverlay.remove(barcode.getKey());
        this.barcodes.remove(barcode.getKey());
      }
    }
  }

  private static void logExtrasForTesting(Barcode barcode) {
    if (barcode != null) {
      if (barcode.getBoundingBox() != null) {
        Log.v(
            MANUAL_TESTING_LOG,
            String.format(
                "Detected barcode's bounding box: %s", barcode.getBoundingBox().flattenToString()));
      }
      if (barcode.getCornerPoints() != null) {
        Log.v(
            MANUAL_TESTING_LOG,
            String.format(
                "Expected corner point size is 4, get %d", barcode.getCornerPoints().length));
      }
      for (Point point : barcode.getCornerPoints()) {
        Log.v(
            MANUAL_TESTING_LOG,
            String.format("Corner point is located at: x = %d, y = %d", point.x, point.y));
      }
      Log.v(MANUAL_TESTING_LOG, "barcode display value: " + barcode.getDisplayValue());
      Log.v(MANUAL_TESTING_LOG, "barcode raw value: " + barcode.getRawValue());

    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Barcode detection failed " + e);
  }
}
