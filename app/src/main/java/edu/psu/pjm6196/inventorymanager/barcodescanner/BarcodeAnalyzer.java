package edu.psu.pjm6196.inventorymanager.barcodescanner;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

import edu.psu.pjm6196.inventorymanager.R;

public class BarcodeAnalyzer implements ImageAnalysis.Analyzer {
    public static final int res_height = 1280;
    public static final int res_width = 720;
    private final String TAG = "BarcodeAnalyzer";

    Paint redBox;
    Paint greenBox;
    Paint blueBox;
    private final Activity activity;

    public BarcodeAnalyzer(Activity activity) {
        this.activity = activity;

        // setup paint
        redBox = new Paint();
        redBox.setAntiAlias(true);
        redBox.setDither(true);
        redBox.setColor(Color.RED);
        redBox.setStyle(Paint.Style.FILL_AND_STROKE);
        redBox.setStrokeWidth(5);

        greenBox = new Paint(redBox);
        greenBox.setColor(Color.GREEN);
        greenBox.setStyle(Paint.Style.STROKE);

        blueBox = new Paint(greenBox);
        blueBox.setColor(Color.BLUE);
    }

    private int scale_val(int val, int from_max, int to_max) {
        return val * from_max / to_max;
    }

    private Point scale_point(Point original, Rect from_size, Rect to_size) {
        int fw = from_size.right;
        int fh = from_size.bottom;
        int tw = to_size.right;
        int th = to_size.bottom;

        return new Point(
            scale_val(original.x, fw, tw),
            scale_val(original.y, fh, th)
        );
    }

    private Rect scale_rect(Rect original, Rect from_size, Rect to_size) {
        int fw = from_size.right;
        int fh = from_size.bottom;
        int tw = to_size.right;
        int th = to_size.bottom;

        return  new Rect(
            scale_val(original.left, fw, tw),
            scale_val(original.top, fh, th),
            scale_val(original.right, fw, tw),
            scale_val(original.bottom, fh, th)
        );
    }

    @Override
    @OptIn(markerClass = ExperimentalGetImage.class)
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image =
                InputImage.fromBitmap(imageProxy.toBitmap(), imageProxy.getImageInfo().getRotationDegrees());

            BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                    // TODO: config idea: multiple barcode formats
                    .setBarcodeFormats( Barcode.FORMAT_PDF417 )
                    .build();
            BarcodeScanner scanner = BarcodeScanning.getClient(options);

            scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        SurfaceView surfaceView = activity.findViewById(R.id.surfaceView);
                        surfaceView.setZOrderOnTop(true);

                        SurfaceHolder surface = surfaceView.getHolder();
                        surface.setFormat(PixelFormat.TRANSPARENT);

                        Rect size = surface.getSurfaceFrame();
                        Rect image_size = imageProxy.getCropRect();
                        int w = imageProxy.getWidth();
                        int h = imageProxy.getHeight();
//                        Rect image_size = new Rect(0, 0, w, h);
//                        Log.d(TAG, String.format("Surface size: h(%s, %s) v(%s, %s)", size.left, size.right, size.top, size.bottom));
//                        Log.d(TAG, String.format("Image size: (%s, %s)", image.getWidth(), image.getHeight()));

                        Canvas canvas;

                        // TODO: store barcodes locations in a HashMap for canvas rendering
                        //      - key is barcode data
                        //      - value is corners (or maybe a custom class with width height and rotation)
                        if ( barcodes.size() > 0) {
                            canvas = surface.lockCanvas();

                            // clear canvas
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                            for (Barcode barcode: barcodes) {
                                Rect bounds = barcode.getBoundingBox();
                                Point[] corners = barcode.getCornerPoints();

                                if ( bounds != null) {
                                    Rect new_bounds = scale_rect(bounds, size, image_size);
                                    Log.d(TAG, String.format("Old Bounds: h(%s, %s) v(%s, %s)", bounds.left, bounds.right, bounds.top, bounds.bottom));
                                    Log.d(TAG, String.format("New Bounds: h(%s, %s) v(%s, %s)", new_bounds.left, new_bounds.right, new_bounds.top, new_bounds.bottom));

                                    canvas.drawRect(bounds, redBox);
                                    canvas.drawRect(new_bounds, greenBox);
                                }

                                if ( corners != null ) {
//                                        float[] pts = new float[corners.length * 2];
//                                        for (int i=0; i<corners.length; i++) {
//                                            pts[i] = corners[i].x;
//                                            pts[i+1] = corners[i].y;
//                                        }
//                                        canvas.drawLines(pts, blueBox);

                                    Point[] pts = new Point[corners.length];
                                    int i;
                                    for (i = 0; i < corners.length; i++)
                                        pts[i] = scale_point(corners[i], size, image_size);

                                    for (i = 0; i < pts.length-1; i++)
                                        canvas.drawLine(
                                            pts[i].x,   pts[i].y,
                                            pts[i+1].x, pts[i+1].y,
                                            blueBox
                                        );
                                    canvas.drawLine(
                                        pts[i].x,   pts[i].y,
                                        pts[0].x, pts[0].y,
                                        blueBox
                                    );
                                }

                                String rawValue = barcode.getRawValue();
                                int valueType = barcode.getValueType();
                                // See API reference for complete list of supported types
                                if (valueType == Barcode.TYPE_TEXT) {
                                    Log.d(TAG, "Scanned barcode: " + rawValue);
//                                    Toast.makeText(ScanActivity.this, rawValue, Toast.LENGTH_LONG).show();
                                } else {
                                    Log.e(TAG, "Got other barcode type: " + valueType);
                                }
                            }

                            surface.unlockCanvasAndPost(canvas);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // no barcode: maybe do something at some timeout?
                    }
                });

            imageProxy.close();
        }
    }
}
