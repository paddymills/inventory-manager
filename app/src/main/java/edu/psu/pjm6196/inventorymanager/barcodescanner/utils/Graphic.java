package edu.psu.pjm6196.inventorymanager.barcodescanner.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.google.common.primitives.Ints;

/**
 * Base class for a custom graphics object to be rendered within the graphic overlay. Subclass
 * this and implement the {@link Graphic#draw(Canvas)} method to define the graphics element. Add
 * instances to the overlay using {@link GraphicOverlay#add(String, Graphic)}.
 */
public abstract class Graphic {
    private final GraphicOverlay overlay;

    public Graphic(GraphicOverlay overlay) {
        this.overlay = overlay;
    }

    /**
     * Draw the graphic on the supplied canvas. Drawing should use the following methods to convert
     * to view coordinates for the graphics that are drawn:
     *
     * <ol>
     *   <li>{@link Graphic#scale(float)} adjusts the size of the supplied value from the image
     *       scale to the view scale.
     *   <li>{@link Graphic#translateX(float)} and {@link Graphic#translateY(float)} adjust the
     *       coordinate from the image's coordinate system to the view coordinate system.
     * </ol>
     *
     * @param canvas drawing canvas
     */
    public abstract void draw(Canvas canvas);

    protected void drawRect(
        Canvas canvas, float left, float top, float right, float bottom, Paint paint) {
        canvas.drawRect(left, top, right, bottom, paint);
    }

    protected void drawText(Canvas canvas, String text, float x, float y, Paint paint) {
        canvas.drawText(text, x, y, paint);
    }

    /**
     * Adjusts the supplied value from the image scale to the view scale.
     */
    public float scale(float imagePixel) {
        return imagePixel * overlay.getScaleFactor();
    }

    /**
     * Returns the application context of the app.
     */
    public Context getApplicationContext() {
        return overlay.getContext().getApplicationContext();
    }

    public boolean isImageFlipped() {
        return overlay.isImageFlipped();
    }

    /**
     * Adjusts the x coordinate from the image's coordinate system to the view coordinate system.
     */
    public float translateX(float x) {
        if (overlay.isImageFlipped()) {
            return overlay.getWidth() - (scale(x) - overlay.getPostScaleWidthOffset());
        } else {
            return scale(x) - overlay.getPostScaleWidthOffset();
        }
    }

    /**
     * Adjusts the y coordinate from the image's coordinate system to the view coordinate system.
     */
    public float translateY(float y) {
        return scale(y) - overlay.getPostScaleHeightOffset();
    }

    /**
     * Returns a {@link Matrix} for transforming from image coordinates to overlay view coordinates.
     */
    public Matrix getTransformationMatrix() {
        return overlay.getTransformationMatrix();
    }

    public void postInvalidate() {
        overlay.postInvalidate();
    }

    /**
     * Given the {@code zInImagePixel}, update the color for the passed in {@code paint}. The color will be
     * more red if the {@code zInImagePixel} is smaller, or more blue ish vice versa. This is
     * useful to visualize the z value of landmarks via color for features like Pose and Face Mesh.
     *
     * @param paint                    the paint to update color with
     * @param canvas                   the canvas used to draw with paint
     * @param visualizeZ               if true, paint color will be changed.
     * @param rescaleZForVisualization if true, re-scale the z value with zMin and zMax to make
     *                                 color more distinguishable
     * @param zInImagePixel            the z value used to update the paint color
     * @param zMin                     min value of all z values going to be passed in
     * @param zMax                     max value of all z values going to be passed in
     */
    public void updatePaintColorByZValue(
        Paint paint,
        Canvas canvas,
        boolean visualizeZ,
        boolean rescaleZForVisualization,
        float zInImagePixel,
        float zMin,
        float zMax) {
        if (!visualizeZ) {
            return;
        }

        // When visualizeZ is true, sets up the paint to different colors based on z values.
        // Gets the range of z value.
        float zLowerBoundInScreenPixel;
        float zUpperBoundInScreenPixel;

        if (rescaleZForVisualization) {
            zLowerBoundInScreenPixel = min(-0.001f, scale(zMin));
            zUpperBoundInScreenPixel = max(0.001f, scale(zMax));
        } else {
            // By default, assume the range of z value in screen pixel is [-canvasWidth, canvasWidth].
            float defaultRangeFactor = 1f;
            zLowerBoundInScreenPixel = -defaultRangeFactor * canvas.getWidth();
            zUpperBoundInScreenPixel = defaultRangeFactor * canvas.getWidth();
        }

        float zInScreenPixel = scale(zInImagePixel);

        if (zInScreenPixel < 0) {
            // Sets up the paint to be red if the item is in front of the z origin.
            // Maps values within [zLowerBoundInScreenPixel, 0) to [255, 0) and use it to control the
            // color. The larger the value is, the more red it will be.
            int v = (int) (zInScreenPixel / zLowerBoundInScreenPixel * 255);
            v = Ints.constrainToRange(v, 0, 255);
            paint.setARGB(255, 255, 255 - v, 255 - v);
        } else {
            // Sets up the paint to be blue if the item is behind the z origin.
            // Maps values within [0, zUpperBoundInScreenPixel] to [0, 255] and use it to control the
            // color. The larger the value is, the more blue it will be.
            int v = (int) (zInScreenPixel / zUpperBoundInScreenPixel * 255);
            v = Ints.constrainToRange(v, 0, 255);
            paint.setARGB(255, 255 - v, 255 - v, 255);
        }
    }
}
