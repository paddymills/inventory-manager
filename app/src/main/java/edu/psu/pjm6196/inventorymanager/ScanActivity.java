package edu.psu.pjm6196.inventorymanager;

import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.mlkit.vision.demo.CameraXViewModel;

import android.Manifest;
import android.util.Log;

import edu.psu.pjm6196.inventorymanager.barcodescanner.BarcodeScannerProcessor;
import edu.psu.pjm6196.inventorymanager.barcodescanner.utils.GraphicOverlay;

public class ScanActivity extends CustomAppCompatActivity {

    // some of this code was adapted from the blog post: https://medium.com/swlh/introduction-to-androids-camerax-with-java-ca384c522c5
    private static final String TAG = "ScanActivity";

    private static final String[] CAMERA_PERMISSION = new String[] {Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    public static final CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
    public static final int cameraLens = CameraSelector.LENS_FACING_BACK;
    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;

    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private ImageAnalysis imageAnalysis;
    private BarcodeScannerProcessor barcodeProcessor;
    private boolean needUpdateGraphicOverlayImageSourceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Log.i(TAG, "onCreate");

        previewView = findViewById(R.id.scan_preview);
        graphicOverlay = findViewById(R.id.scan_overlay);

        // TODO: handle if user refuses camera permission
        if ( !hasCameraPermission() )
            requestCameraPermission();

        previewView = findViewById(R.id.scan_preview);

        new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
            .get(CameraXViewModel.class)
            .getProcessCameraProvider()
            .observe(
                this,
                provider -> {
                    cameraProvider = provider;
                    bindMlKitCamera();
                });

//        init_camera_old();
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void bindMlKitCamera() {
        if (cameraProvider == null)
            return;

        if (preview != null)
            cameraProvider.unbind(preview);

        preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview);

        if ( imageAnalysis != null )
            cameraProvider.unbind(imageAnalysis);

        if ( barcodeProcessor != null )
            barcodeProcessor.stop();

        barcodeProcessor = new BarcodeScannerProcessor(this);

        imageAnalysis =
            new ImageAnalysis.Builder()
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .setResolutionSelector(
//                    new ResolutionSelector.Builder()
//                        .setResolutionStrategy(
//                            new ResolutionStrategy(new Size(previewView.getWidth(), previewView.getHeight()), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER)
//                        )
//                        .build()
//                )
                .build();

        needUpdateGraphicOverlayImageSourceInfo = true;
        imageAnalysis.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(this),
            imageProxy -> {
                if (needUpdateGraphicOverlayImageSourceInfo) {
//                    boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT;
                    boolean isImageFlipped = false;
                    int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        graphicOverlay.setImageSourceInfo(
                            imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                    } else {
                        graphicOverlay.setImageSourceInfo(
                            imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false;
                }

                barcodeProcessor.processImageProxy(imageProxy, graphicOverlay);
            });

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis);
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, CAMERA_REQUEST_CODE);
    }
}