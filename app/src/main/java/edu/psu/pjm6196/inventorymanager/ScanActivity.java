package edu.psu.pjm6196.inventorymanager;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import android.Manifest;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ScanActivity extends CustomAppCompatActivity {

    // some of this code was adapted from the blog post: https://medium.com/swlh/introduction-to-androids-camerax-with-java-ca384c522c5
    private final String TAG = "ScanActivity";

    private static final String[] CAMERA_PERMISSION = new String[] {Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // TODO: handle if user refuses camera permission
        if ( !hasCameraPermission() )
            requestCameraPermission();

        findViewById(R.id.btn_scan).setOnClickListener(v -> scanBarcode());

        previewView = findViewById(R.id.scan_preview);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindImageAnalysis(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, CAMERA_REQUEST_CODE);
    }

    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        ImageAnalysis imageAnalysis =
            new ImageAnalysis.Builder()
                .setResolutionSelector(
                    new ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            new ResolutionStrategy(new Size(1280, 720), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER)
                        )
                        .build()
                )
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                new BarcodeAnalyzer().analyze(image);
                image.close();
            }
        });

        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }

    public void scanBarcode() {
        Log.d(TAG, "Scanning barcode...");


    }

    class BarcodeAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        @OptIn(markerClass = ExperimentalGetImage.class)
        public void analyze(@NonNull ImageProxy imageProxy) {
            Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image =
                        InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                BarcodeScannerOptions options =
                    new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                            Barcode.FORMAT_PDF417)
                        .build();
                BarcodeScanner scanner = BarcodeScanning.getClient(options);

                scanner.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            for (Barcode barcode: barcodes) {
                                Rect bounds = barcode.getBoundingBox();
                                Point[] corners = barcode.getCornerPoints();

                                String rawValue = barcode.getRawValue();

                                int valueType = barcode.getValueType();
                                // See API reference for complete list of supported types
                                if (valueType == Barcode.TYPE_TEXT) {
                                    Log.d(TAG, "Scanned barcode: " + rawValue);
                                    Toast.makeText(ScanActivity.this, rawValue, Toast.LENGTH_LONG).show();
                                } else {
                                    Log.e(TAG, "Got other barcode type: " + valueType);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // no barcode: maybe do something at some timeout?
                        }
                    });
                // Pass image to an ML Kit Vision API
                // ...
            }

        }
    }
}