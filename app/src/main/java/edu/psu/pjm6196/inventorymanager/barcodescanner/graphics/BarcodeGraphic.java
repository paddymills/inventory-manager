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

package edu.psu.pjm6196.inventorymanager.barcodescanner.graphics;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.barcode.common.Barcode;

/** Graphic instance for rendering Barcode position and content information in an overlay view. */
public class BarcodeGraphic extends Graphic {
  public static long LIFETIME_DURATION = 1000;

  private static int UNSELECTED_COLOR = BarcodeGraphic.MARKER_COLOR;
  private static int SELECTED_COLOR = Color.GREEN;

  protected static final int TEXT_COLOR = Color.BLACK;
  protected static final int MARKER_COLOR = Color.WHITE;
  protected static final float TEXT_SIZE = 54.0f;
  protected static final float STROKE_WIDTH = 4.0f;


  protected Barcode barcode;
  private final long lastScannedTime;
  private boolean isSelected;
  private RectF lastDrawRect;

  public BarcodeGraphic(GraphicOverlay overlay, Barcode barcode) {
    super(overlay);

    this.barcode = barcode;
    this.lastScannedTime = System.currentTimeMillis();
  }

  @NonNull
  @Override
  public String toString() {
    if ( this.barcode != null )
      return this.barcode.getDisplayValue();

    return "";
  }

  public void setBarcode(Barcode barcode) {
    this.barcode = barcode;
  }

  public boolean isSelected() {
    return isSelected;
  }
  public void toggleSelected() {
    isSelected = !isSelected;
  }
  public void clearSelected() {
    isSelected = false;
  }
  public boolean isTouchInRect(float x, float y) {
    // get barcode rectangle
    RectF rect = calculateRect();
    RectF labelRect = getTextBackgroundRect(rect);

    return rect.contains(x, y) || labelRect.contains(x, y);
  }

  public boolean isActive() {
    return System.currentTimeMillis() - this.lastScannedTime > LIFETIME_DURATION;
  }


  /**
   * @return if the last drawn RectF differs from the current calculated RectF
   */
  public boolean needsReDrawn() {
    return calculateRect().equals(lastDrawRect);
  }

  protected int getColor() {
    return isSelected ? SELECTED_COLOR : UNSELECTED_COLOR;
  }

  protected Paint getRectPaint() {
    Paint paint = new Paint();

    paint.setColor(getColor());
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(BarcodeGraphic.STROKE_WIDTH);

    return paint;
  }

  protected Paint getBarcodePaint() {
    Paint paint = new Paint();

    paint.setColor(BarcodeGraphic.TEXT_COLOR);
    paint.setTextSize(TEXT_SIZE);

    return paint;
  }

  protected Paint getLabelPaint() {
    Paint paint = new Paint();

    paint.setColor(getColor());
    paint.setStyle(Paint.Style.FILL);

    return paint;
  }

  private RectF calculateRect() {
    // calculate bounding box around the BarcodeBlock.
    RectF rect = new RectF(barcode.getBoundingBox());

    // If the image is flipped, the left will be translated to right, and the right to left.
    float x0 = translateX(rect.left);
    float x1 = translateX(rect.right);
    rect.left = min(x0, x1);
    rect.right = max(x0, x1);
    rect.top = translateY(rect.top);
    rect.bottom = translateY(rect.bottom);

    return rect;
  }

  private RectF getTextBackgroundRect(RectF rect) {
    float lineHeight = TEXT_SIZE + (2 * STROKE_WIDTH);
    float textWidth = getBarcodePaint().measureText(barcode.getDisplayValue());

    return new RectF(
        rect.left - STROKE_WIDTH,
        rect.top - lineHeight,
        rect.left + textWidth + (2 * STROKE_WIDTH),
        rect.top
    );
  }

  private RectF getTextBackgroundRect() {
    return getTextBackgroundRect(calculateRect());
  }

  /**
   * Draws the barcode block annotations for position, size, and raw value on the supplied canvas.
   */
  @Override
  public void draw(Canvas canvas) {
    if (barcode == null) {
      throw new IllegalStateException("Attempting to draw a null barcode.");
    }

    // calculate barcode rectangle
    RectF rect = calculateRect();

    // draw rectangle
    canvas.drawRect(rect, getRectPaint());

    // Draws other object info.
    canvas.drawRect( getTextBackgroundRect(rect), getLabelPaint() );

    // Renders the barcode at the bottom of the box.
    canvas.drawText(barcode.getDisplayValue(), rect.left, rect.top - STROKE_WIDTH, getBarcodePaint());

    // save last draw size/location
    lastDrawRect = rect;
  }
}
