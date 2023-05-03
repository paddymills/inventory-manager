package edu.psu.pjm6196.inventorymanager.barcodescanner;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.barcode.common.Barcode;

import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.BarcodeGraphic;
import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.GraphicOverlay;

class ScannedBarcode extends BarcodeGraphic {
    private static final long LIFETIME_DURATION = 1000;

    private static int UNSELECTED_COLOR = BarcodeGraphic.MARKER_COLOR;
    private static int SELECTED_COLOR = Color.GREEN;
    private final long lastScannedTime;
    private boolean isSelected;
    private RectF rect;

    public ScannedBarcode(GraphicOverlay overlay, Barcode barcode) {
        super(overlay, barcode);
        calculateRect();

        this.lastScannedTime = System.currentTimeMillis();
    }

    public void setBarcode(Barcode barcode) {
        this.barcode = barcode;
        calculateRect();
    }

    @NonNull
    @Override
    public String toString() {
        if ( this.barcode != null )
            return this.barcode.getRawValue();

        return "";
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void toggleSelected() {
        isSelected = !isSelected;
    }

    public boolean isTouchInRect(float x, float y) {
        return rect.contains(x, y);
    }

    public boolean needsRemoved() {
        return System.currentTimeMillis() - this.lastScannedTime > LIFETIME_DURATION;
    }

    private void calculateRect() {
        // Draws the bounding box around the BarcodeBlock.
        rect = new RectF(barcode.getBoundingBox());
        // If the image is flipped, the left will be translated to right, and the right to left.
        float x0 = translateX(rect.left);
        float x1 = translateX(rect.right);
        rect.left = min(x0, x1);
        rect.right = max(x0, x1);
        rect.top = translateY(rect.top);
        rect.bottom = translateY(rect.bottom);
    }

    @Override
    protected void setGraphicPaints() {
        int fillColor = this.isSelected ? SELECTED_COLOR : UNSELECTED_COLOR;

        rectPaint = new Paint();
        rectPaint.setColor(fillColor);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(BarcodeGraphic.STROKE_WIDTH);

        barcodePaint = new Paint();
        barcodePaint.setColor(BarcodeGraphic.TEXT_COLOR);
        barcodePaint.setTextSize(TEXT_SIZE);

        labelPaint = new Paint();
        labelPaint.setColor(fillColor);
        labelPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(Canvas canvas) {
        setGraphicPaints();
        super.draw(canvas);
    }
}
