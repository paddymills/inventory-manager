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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

import com.google.mlkit.vision.barcode.common.Barcode;

import edu.psu.pjm6196.inventorymanager.barcodescanner.GraphicOverlay;
import edu.psu.pjm6196.inventorymanager.utils.Quadrilateral;

/**
 * Graphic instance for rendering Barcode position and content information in an overlay view.
 */
public class BarcodeGraphic extends Graphic {
    protected static final int TEXT_COLOR = Color.BLACK;
    protected static final float TEXT_SIZE = 54.0f;
    protected static final float STROKE_WIDTH = 4.0f;
    private static final int UNSELECTED_COLOR = Color.WHITE;
    private static final int SELECTED_COLOR = Color.GREEN;
    protected String label;
    private Barcode barcode;
    private Quadrilateral boundingBox;
    private boolean isSelected;

    public BarcodeGraphic(Barcode barcode, boolean selected) {
        this.label = barcode.getDisplayValue();
        this.barcode = barcode;

        this.isSelected = selected;
    }

    public void setBarcode(Barcode barcode) {
        this.barcode = barcode;
        this.label = barcode.getDisplayValue();
    }

    public String getLabel() {
        return this.label;
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
        if (boundingBox == null)
            return false;

        return boundingBox.isPointWithin(new Point((int) x, (int) y));
    }

    protected int getColor() {
        return isSelected ? SELECTED_COLOR : UNSELECTED_COLOR;
    }

    protected Paint getBoxPaint() {
        Paint paint = new Paint();

        paint.setColor(getColor());
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(BarcodeGraphic.STROKE_WIDTH);

        return paint;
    }

    protected Paint getLabelPaint() {
        Paint paint = new Paint();

        paint.setColor(BarcodeGraphic.TEXT_COLOR);
        paint.setTextSize(TEXT_SIZE);

        return paint;
    }

    protected Paint getLabelBackgroundPaint() {
        Paint paint = new Paint();

        paint.setColor(getColor());
        paint.setStyle(Paint.Style.FILL);

        return paint;
    }

    private Quadrilateral calculateQuad(GraphicOverlay overlay) {
        Quadrilateral quad = new Quadrilateral(barcode);

        quad.translate(p -> {
            p.x = (int) overlay.translateX(p.x);
            p.y = (int) overlay.translateY(p.y);

            return p;
        });

        return quad;
    }

    private RectF getTextBackgroundRect(RectF rect) {
        float lineHeight = TEXT_SIZE + (2 * STROKE_WIDTH);
        float textWidth = getLabelPaint().measureText(label);

        return new RectF(
            rect.left - STROKE_WIDTH,
            rect.top - lineHeight,
            rect.left + textWidth + (2 * STROKE_WIDTH),
            rect.top
        );
    }

    /**
     * Draws the barcode block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas, GraphicOverlay overlay) {
        boundingBox = calculateQuad(overlay);

        // draw quadrilateral
        boundingBox.draw(canvas, getBoxPaint());

        // Draws other object info.
//        canvas.drawRect(getTextBackgroundRect(rect), getLabelBackgroundPaint());

        // Renders the barcode at the top of the box.
        Point anchor = boundingBox.getAnchor();
        canvas.drawText(label, anchor.x, anchor.y - STROKE_WIDTH, getLabelPaint());
    }
}
