package edu.psu.pjm6196.inventorymanager;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;

import com.google.common.util.concurrent.ListenableFuture;
import android.Manifest;
import android.view.SurfaceView;
import android.widget.Button;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class ScanActivity extends CustomAppCompatActivity {

    // some of this code was adapted from the blog post: https://medium.com/swlh/introduction-to-androids-camerax-with-java-ca384c522c5
    private final String TAG = "ScanActivity";

    private static final String[] CAMERA_PERMISSION = new String[] {Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    private boolean scanning_is_bound;


    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // TODO: handle if user refuses camera permission
        if ( !hasCameraPermission() )
            requestCameraPermission();

        findViewById(R.id.btn_scan).setOnClickListener(v -> toggle_scanner_binding());

        previewView = findViewById(R.id.scan_preview);
//        previewView.setScaleType(PreviewView.ScaleType.FIT_CENTER);
        previewView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        Executor executor = ContextCompat.getMainExecutor(this);
        cameraProviderFuture.addListener(this::toggle_scanner_binding, executor);
    }

    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        ImageAnalysis imageAnalysis =
            new ImageAnalysis.Builder()
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
//                .setImageQueueDepth(3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setResolutionSelector(
                    new ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            new ResolutionStrategy(new Size(previewView.getWidth(), previewView.getHeight()), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER)
                        )
                        .build()
                )
                .build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new BarcodeAnalyzer(this));

        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }

    private void toggle_scanner_binding() {
        try {
            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
            if ( scanning_is_bound ) {
                Log.d(TAG, "Scanning barcode...");

                SurfaceView surfaceView = findViewById(R.id.surfaceView);
                Canvas canvas = surfaceView.getHolder().lockCanvas();
                canvas.drawBitmap(previewView.getBitmap(), canvas.getMatrix(), new Paint());
                surfaceView.getHolder().unlockCanvasAndPost(canvas);

                cameraProvider.unbindAll();
                ((Button) findViewById(R.id.btn_scan)).setText(R.string.scan_capture_unbound);
            } else {
                bindImageAnalysis(cameraProvider);
                ((Button) findViewById(R.id.btn_scan)).setText(R.string.scan_capture_bound);
            }

            scanning_is_bound = !scanning_is_bound;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, CAMERA_REQUEST_CODE);
    }
}