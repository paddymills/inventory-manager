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
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.google.common.base.Preconditions;

import java.util.HashMap;

import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.BarcodeGraphic;
import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.CameraImageGraphic;
import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.Graphic;
import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.TelemetryInfoGraphic;
import edu.psu.pjm6196.inventorymanager.barcodescanner.utils.TransformationHandler;
import edu.psu.pjm6196.inventorymanager.utils.PreferenceUtils;

/**
 * A view which renders a series of custom graphics to be overlayed on top of an associated preview
 * (i.e., the camera preview). The creator can add graphics objects, update the objects, and remove
 * them, triggering the appropriate drawing and invalidation within the view.
 *
 * <p>Supports scaling and mirroring of the graphics relative the camera's preview properties. The
 * idea is that detection items are expressed in terms of an image size, but need to be scaled up to
 * the full view size, and also mirrored in the case of the front-facing camera.
 *
 * <p>Associated {@link Graphic} items should use the following methods to convert to view
 * coordinates for the graphics that are drawn:
 *
 * <ol>
 *   <li>{@link GraphicOverlay#scale(float)} adjusts the size of the supplied value from the image scale to
 *       the view scale.
 *   <li>{@link GraphicOverlay#translateX(float)} and {@link GraphicOverlay#translateY(float)} adjust the
 *       coordinate from the image's coordinate system to the view coordinate system.
 * </ol>
 */
public class GraphicOverlay extends View implements TransformationHandler {
    private static final String TAG = "GraphicOverlay";
    private final Object lock = new Object();
    private final HashMap<String, Graphic> graphics = new HashMap<>();
    // Matrix for transforming from image coordinates to overlay view coordinates.
    private final Matrix transformationMatrix = new Matrix();

    private int imageWidth;
    private int imageHeight;
    // The factor of overlay View size to image size. Anything in the image coordinates need to be
    // scaled by this amount to fit with the area of overlay View.
    private float scaleFactor = 1.0f;
    // The number of horizontal pixels needed to be cropped on each side to fit the image with the
    // area of overlay View after scaling.
    private float postScaleWidthOffset;
    // The number of vertical pixels needed to be cropped on each side to fit the image with the
    // area of overlay View after scaling.
    private float postScaleHeightOffset;
    private boolean isImageFlipped;
    private boolean needUpdateTransformation = true;
    private TelemetryInfoGraphic telemetry;
    private CameraImageGraphic cameraImage;
    private ScannedBarcodeHandler listener;

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        addOnLayoutChangeListener(
            (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                needUpdateTransformation = true);
    }

    public void setDataListener(ScannedBarcodeHandler listener) {
        this.listener = listener;
    }

    /**
     * @return a {@link Matrix} for transforming from image coordinates to overlay view coordinates.
     */
    public Matrix getTransformationMatrix() {
        return this.transformationMatrix;
    }

    public float getScaleFactor() {
        return this.scaleFactor;
    }

    public float getPostScaleWidthOffset() {
        return this.postScaleWidthOffset;
    }

    public float getPostScaleHeightOffset() {
        return this.postScaleHeightOffset;
    }

    public boolean isImageFlipped() {
        return this.isImageFlipped;
    }

    /**
     * Removes all graphics from the overlay.
     */
    public void clear() {
        synchronized (lock) {
            graphics.clear();
        }
        postInvalidate();
    }

    /**
     * Adds a graphic to the overlay.
     */
    public void add(String key, Graphic graphic) {
        synchronized (lock) {
            graphics.put(key, graphic);
        }
    }

    /**
     * Adds a telemetry graphic to the overlay.
     */
    public void updateImage(CameraImageGraphic image) {
        synchronized (lock) {
            this.cameraImage = image;
            postInvalidate();
        }
    }

    /**
     * Adds a telemetry graphic to the overlay.
     */
    public void updateTelemetry(TelemetryInfoGraphic telemetry) {
        synchronized (lock) {
            this.telemetry = telemetry;
            postInvalidate();
        }
    }

    /**
     * Sets the source information of the image being processed by detectors, including size and
     * whether it is flipped, which informs how to transform image coordinates later.
     *
     * @param imageWidth  the width of the image sent to ML Kit detectors
     * @param imageHeight the height of the image sent to ML Kit detectors
     * @param isFlipped   whether the image is flipped. Should set it to true when the image is from the
     *                    front camera.
     */
    public void setImageSourceInfo(int imageWidth, int imageHeight, boolean isFlipped) {
        Preconditions.checkState(imageWidth > 0, "image width must be positive");
        Preconditions.checkState(imageHeight > 0, "image height must be positive");
        synchronized (lock) {
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            this.isImageFlipped = isFlipped;
            needUpdateTransformation = true;
        }
        postInvalidate();
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    private void updateTransformationIfNeeded() {
        if (!needUpdateTransformation || imageWidth <= 0 || imageHeight <= 0) {
            return;
        }
        float viewAspectRatio = (float) getWidth() / getHeight();
        float imageAspectRatio = (float) imageWidth / imageHeight;
        postScaleWidthOffset = 0;
        postScaleHeightOffset = 0;
        if (viewAspectRatio > imageAspectRatio) {
            // The image needs to be vertically cropped to be displayed in this view.
            scaleFactor = (float) getWidth() / imageWidth;
            postScaleHeightOffset = ((float) getWidth() / imageAspectRatio - getHeight()) / 2;
        } else {
            // The image needs to be horizontally cropped to be displayed in this view.
            scaleFactor = (float) getHeight() / imageHeight;
            postScaleWidthOffset = ((float) getHeight() * imageAspectRatio - getWidth()) / 2;
        }

        transformationMatrix.reset();
        transformationMatrix.setScale(scaleFactor, scaleFactor);
        transformationMatrix.postTranslate(-postScaleWidthOffset, -postScaleHeightOffset);

        if (isImageFlipped) {
            transformationMatrix.postScale(-1f, 1f, getWidth() / 2f, getHeight() / 2f);
        }

        needUpdateTransformation = false;
    }

    /**
     * Adjusts the supplied value from the image scale to the view scale.
     */
    @Override
    public float scale(float imagePixel) {
        return imagePixel * getScaleFactor();
    }

    /**
     * @return the x coordinate adjusted from the image's coordinate system to the view coordinate system.
     */
    @Override
    public float translateX(float x) {
        if (isImageFlipped()) {
            return getWidth() - (scale(x) - getPostScaleWidthOffset());
        } else {
            return scale(x) - getPostScaleWidthOffset();
        }
    }

    /**
     * @return the y coordinate adjusted from the image's coordinate system to the view coordinate system.
     */
    @Override
    public float translateY(float y) {
        return scale(y) - getPostScaleHeightOffset();
    }

    /**
     * Draws the overlay with its associated graphic objects.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (lock) {
            updateTransformationIfNeeded();

            if (PreferenceUtils.showDetectionInfo(this.getContext()))
                telemetry.draw(canvas, this);

            if (cameraImage != null)
                cameraImage.draw(canvas, this);

            if (listener != null) {
                for (BarcodeGraphic graphic : listener.getBarcodesToRender()) {
                    Log.v(TAG, "Drawing barcode: " + graphic.getLabel());
                    graphic.draw(canvas, this);
                }
            }
        }
    }

    // TODO: OnTouchListener

    @Override
    public boolean performClick() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
        super.performClick();

        return true;
    }
}