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

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.google.mlkit.vision.barcode.common.Barcode;
import edu.psu.pjm6196.inventorymanager.barcodescanner.utils.GraphicOverlay;
import edu.psu.pjm6196.inventorymanager.barcodescanner.utils.Graphic;

/** Graphic instance for rendering Barcode position and content information in an overlay view. */
public class BarcodeGraphic extends Graphic {

  protected static final int TEXT_COLOR = Color.BLACK;
  protected static final int MARKER_COLOR = Color.WHITE;
  protected static final float TEXT_SIZE = 54.0f;
  protected static final float STROKE_WIDTH = 4.0f;

  protected Paint rectPaint;
  protected Paint barcodePaint;
  protected Paint labelPaint;
  protected Barcode barcode;

  BarcodeGraphic(GraphicOverlay overlay, Barcode barcode) {
    super(overlay);

    this.barcode = barcode;
    setGraphicPaints();
  }

  /**
   * Draws the barcode block annotations for position, size, and raw value on the supplied canvas.
   */
  protected void setGraphicPaints() {
    rectPaint = new Paint();
    rectPaint.setColor(MARKER_COLOR);
    rectPaint.setStyle(Paint.Style.STROKE);
    rectPaint.setStrokeWidth(STROKE_WIDTH);

    barcodePaint = new Paint();
    barcodePaint.setColor(TEXT_COLOR);
    barcodePaint.setTextSize(TEXT_SIZE);

    labelPaint = new Paint();
    labelPaint.setColor(MARKER_COLOR);
    labelPaint.setStyle(Paint.Style.FILL);
  }
  @Override
  public void draw(Canvas canvas) {
    if (barcode == null) {
      throw new IllegalStateException("Attempting to draw a null barcode.");
    }

    // Draws the bounding box around the BarcodeBlock.
    RectF rect = new RectF(barcode.getBoundingBox());
    // If the image is flipped, the left will be translated to right, and the right to left.
    float x0 = translateX(rect.left);
    float x1 = translateX(rect.right);
    rect.left = min(x0, x1);
    rect.right = max(x0, x1);
    rect.top = translateY(rect.top);
    rect.bottom = translateY(rect.bottom);
    canvas.drawRect(rect, rectPaint);

    // Draws other object info.
    float lineHeight = TEXT_SIZE + (2 * STROKE_WIDTH);
    float textWidth = barcodePaint.measureText(barcode.getDisplayValue());
    canvas.drawRect(
        rect.left - STROKE_WIDTH,
        rect.top - lineHeight,
        rect.left + textWidth + (2 * STROKE_WIDTH),
        rect.top,
        labelPaint);
    // Renders the barcode at the bottom of the box.
    canvas.drawText(barcode.getDisplayValue(), rect.left, rect.top - STROKE_WIDTH, barcodePaint);
  }
}
