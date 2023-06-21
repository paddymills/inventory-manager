package edu.psu.pjm6196.inventorymanager.barcodescanner.graphics;

import android.graphics.Canvas;

import edu.psu.pjm6196.inventorymanager.barcodescanner.GraphicOverlay;

/**
 * Base class for a custom graphics object to be rendered within the graphic overlay. Subclass
 * this and implement the {@link Graphic#draw(Canvas, GraphicOverlay)} method to define the graphics element. Add
 * instances to the overlay using {@link GraphicOverlay#add(String, Graphic)}.
 */
public abstract class Graphic {

    /**
     * Draw the graphic on the supplied canvas. Drawing should use the following methods to convert
     * to view coordinates for the graphics that are drawn:
     *
     * <ol>
     *   <li>{@link GraphicOverlay#scale(float)} adjusts the size of the supplied value from the image
     *       scale to the view scale.
     *   <li>{@link GraphicOverlay#translateX(float)} and {@link GraphicOverlay#translateY(float)} adjust the
     *       coordinate from the image's coordinate system to the view coordinate system.
     * </ol>
     *
     * @param canvas drawing canvas
     */
    public abstract void draw(Canvas canvas, GraphicOverlay overlay);
}
