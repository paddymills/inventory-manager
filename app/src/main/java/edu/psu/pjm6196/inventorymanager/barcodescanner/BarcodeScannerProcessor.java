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
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.BarcodeGraphic;
import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.GraphicOverlay;

/** Barcode Detector Demo. */
public class BarcodeScannerProcessor extends VisionProcessorBase<List<Barcode>> {
  // TODO: handle already scanned items in take inventory process

  private static final String TAG = "BarcodeProcessor";

  private final BarcodeScanner barcodeScanner;

  private final ConcurrentHashMap<String, BarcodeGraphic> barcodes;

  public static void set_barcode_lifetime(long lifetime) {
    BarcodeGraphic.LIFETIME_DURATION = lifetime;
  }

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

  public ArrayList<String> handleTouchEvent(MotionEvent touch) {
    ArrayList<String> touchedBarcodes = new ArrayList<>();

    float x = touch.getX();
    float y = touch.getY();
    for (Map.Entry<String, BarcodeGraphic> entry : barcodes.entrySet()) {
      if ( entry.getValue().isTouchInRect(x, y) ) {
        Log.d(TAG, String.format("Barcode %s selected", entry.getKey()));

        touchedBarcodes.add(entry.getKey());
      }
    }

    return touchedBarcodes;
  }

  public void clearSelected() {
    for ( BarcodeGraphic barcode : barcodes.values() ) {
      barcode.clearSelected();
    }
  }

  public void commitBarcodeTouchEvents(List<String> touchedBarcodes) {
    for ( String key : touchedBarcodes ) {
      if ( barcodes.containsKey(key) )
        barcodes.get(key).toggleSelected();
    }
  }

  public int getToBeNumberOfBarcodesSelected(List<String> touchedBarcodes) {
    int selected = getNumberOfBarcodesSelected();

    for ( String key : touchedBarcodes ) {
      if ( barcodes.containsKey(key) && barcodes.get(key).isSelected() )
        selected -= 1;
    }

    return selected;
  }

  private Stream<BarcodeGraphic> getSelectedBarcodes() {
    return barcodes.values().stream().filter(BarcodeGraphic::isSelected);
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
//      Log.v(MANUAL_TESTING_LOG, "No barcode has been detected");
      validateBarcodesDisplayed(graphicOverlay);
    }

    for (int i = 0; i < detectedBarcodes.size(); ++i) {
      Barcode barcode = detectedBarcodes.get(i);

      String key = barcode.getRawValue();
      BarcodeGraphic sb = new BarcodeGraphic(graphicOverlay, barcode);

      assert key != null;
      BarcodeGraphic currentVal = this.barcodes.putIfAbsent(key, sb);
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

  @Override
  protected boolean requiresReDraw() {
    for( BarcodeGraphic barcode : barcodes.values() ) {
      if ( barcode.isActive() && barcode.needsReDrawn() )
        return true;
    }

    return false;
  }

  public void validateBarcodesDisplayed(@NonNull GraphicOverlay graphicOverlay) {
    for ( Map.Entry<String, BarcodeGraphic> barcode : this.barcodes.entrySet() ) {
      if ( !barcode.getValue().isActive() ) {
        graphicOverlay.remove(barcode.getKey());
      }
    }

    graphicOverlay.remove(VisionProcessorBase.DEBUG_GRAPHIC_KEY);
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
                "Expected corner point size is 4, got %d", barcode.getCornerPoints().length));
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
