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

import androidx.annotation.Nullable;

import edu.psu.pjm6196.inventorymanager.barcodescanner.GraphicOverlay;

/**
 * Graphic instance for rendering telemetry info (latency, FPS, resolution) in an overlay view.
 */
public class TelemetryInfoGraphic extends Graphic {

    private static final int TEXT_COLOR = Color.WHITE;
    private static final float TEXT_SIZE = 60.0f;

    private final Paint textPaint;
    private final long frameLatency;
    private final long detectorLatency;

    // Only valid when a stream of input images is being processed. Null for single image mode.
    @Nullable
    private final Integer framesPerSecond;
    private boolean showLatencyInfo = true;

    public TelemetryInfoGraphic(long frameLatency, long detectorLatency, @Nullable Integer framesPerSecond) {
        this.frameLatency = frameLatency;
        this.detectorLatency = detectorLatency;
        this.framesPerSecond = framesPerSecond;
        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setShadowLayer(5.0f, 0f, 0f, Color.BLACK);
    }

    /**
     * Creates an {@link TelemetryInfoGraphic} to only display image size.
     */
    public TelemetryInfoGraphic() {
        this(0, 0, null);
        showLatencyInfo = false;
    }

    @Override
    public synchronized void draw(Canvas canvas, GraphicOverlay overlay) {
        float x = TEXT_SIZE * 0.5f;
        float y = TEXT_SIZE * 1.5f;

        canvas.drawText(
            "InputImage size: "
                + overlay.getImageHeight()
                + "x"
                + overlay.getImageWidth(),
            x,
            y,
            textPaint);

        if (!showLatencyInfo) {
            return;
        }
        // Draw FPS (if valid) and inference latency
        if (framesPerSecond != null) {
            canvas.drawText(
                "FPS: " + framesPerSecond + ", Frame latency: " + frameLatency + " ms",
                x,
                y + TEXT_SIZE,
                textPaint);
        } else {
            canvas.drawText("Frame latency: " + frameLatency + " ms", x, y + TEXT_SIZE, textPaint);
        }
        canvas.drawText(
            "Detector latency: " + detectorLatency + " ms", x, y + TEXT_SIZE * 2, textPaint);
    }
}