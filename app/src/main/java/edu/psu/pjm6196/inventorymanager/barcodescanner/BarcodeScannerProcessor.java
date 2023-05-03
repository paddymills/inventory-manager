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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import edu.psu.pjm6196.inventorymanager.barcodescanner.utils.GraphicOverlay;

/** Barcode Detector Demo. */
public class BarcodeScannerProcessor extends VisionProcessorBase<List<Barcode>> {
  // TODO: handle already scanned items in take inventory process

  private static final String TAG = "BarcodeProcessor";

  private final BarcodeScanner barcodeScanner;

  private final ConcurrentHashMap<String, ScannedBarcode> barcodes;

  public BarcodeScannerProcessor(Context context) {
    super(context);
    barcodes = new ConcurrentHashMap<>();

    barcodeScanner = BarcodeScanning.getClient(
        // detection is much faster if we specify the format, but this is optional
        new BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_PDF417)
            .build()
    );
  }

  public boolean handleTouchEvent(float x, float y) {
    boolean barcodeTouchOccurred = false;

    for (ScannedBarcode barcode : barcodes.values()) {
      if (barcode.isTouchInRect(x, y)) {
        barcode.toggleSelected();
        Log.d(TAG, "Barcode " + barcode.barcode.getRawValue() + " selected");

        barcodeTouchOccurred = true;
      }
    }

    return barcodeTouchOccurred;
  }

  private Stream<ScannedBarcode> selectedBarcodes() {
    return barcodes.values().stream().filter(ScannedBarcode::isSelected);
  }
  public long getNumberOfBarcodesSelected() {
    return selectedBarcodes().count();
  }

  public String[] getSelectedBarcodeIds() {
    return (String[]) selectedBarcodes().map(x -> x.barcode.getRawValue()).toArray();
  }

  public String getSelectedBarcodeId() {
    assert getNumberOfBarcodesSelected() == 1;

    return getSelectedBarcodeIds()[0];
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
  protected void onSuccess(@NonNull List<Barcode> detectedBarcodes, @NonNull GraphicOverlay graphicOverlay) {
    if (detectedBarcodes.isEmpty()) {
      Log.v(MANUAL_TESTING_LOG, "No barcode has been detected");
      validateBarcodesDisplayed(graphicOverlay);
    }

    for (int i = 0; i < detectedBarcodes.size(); ++i) {
      Barcode barcode = detectedBarcodes.get(i);

      String key = barcode.getRawValue();
      ScannedBarcode sb = new ScannedBarcode(graphicOverlay, barcode);

      ScannedBarcode currentVal = this.barcodes.putIfAbsent(key, sb);
      if ( currentVal != null ) {
        // barcode exists -> retain existing attributes
        currentVal.setBarcode(barcode);
        sb = currentVal;

        this.barcodes.put(key, sb);
      }

      graphicOverlay.add(key, sb);
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
