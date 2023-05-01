package edu.psu.pjm6196.inventorymanager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import edu.psu.pjm6196.inventorymanager.barcodescanner.BarcodeScannerProcessor;
import edu.psu.pjm6196.inventorymanager.barcodescanner.utils.CameraXViewModel;
import edu.psu.pjm6196.inventorymanager.barcodescanner.utils.GraphicOverlay;
import edu.psu.pjm6196.inventorymanager.utils.PreferenceUtils;

public class ScanActivity extends CustomAppCompatActivity {
    private static final String TAG = "ScanActivity";

    private static final String[] CAMERA_PERMISSION = new String[] {Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    public static final int cameraLens = CameraSelector.LENS_FACING_BACK;
    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;

    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private Preview preview;
    private ImageAnalysis imageAnalysis;
    private BarcodeScannerProcessor barcodeProcessor;
    private boolean needUpdateGraphicOverlayImageSourceInfo;
    private boolean scanning_is_paused;
    private CallingActivityIntent scan_use_case;

    // for knowing what the calling activity wants to do with the scanned data barcode(s)
    public enum CallingActivityIntent {
        // single barcode
        AddMaterial,
        FindMaterial,
        MoveMaterial,

        // multiple barcodes
        FilterList,
        TakeInventory;

        public boolean supportsMultipleBarcodeScanning() {
            switch ( this ) {
                case AddMaterial:
                case FindMaterial:
                case MoveMaterial:
                    return true;
            }

            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Log.i(TAG, "onCreate");

        Intent callingIntent = getIntent();
        scan_use_case  = (CallingActivityIntent) callingIntent.getSerializableExtra("calling_activity_intent");

        previewView = findViewById(R.id.scan_preview);
        graphicOverlay = findViewById(R.id.scan_overlay);

        // get camera permissions and display error dialog until the user agrees
        while (cameraPermissionsNotGranted()) {
            requestCameraPermission();

            if (cameraPermissionsNotGranted())
                new ScanActivity.CameraAccessErrorFragment()
                        .show(getSupportFragmentManager(), "CameraAccessError");
        }

        // if we need to dynamically select camera lens use CameraSelector.Builder
        cameraSelector = new CameraSelector.Builder().requireLensFacing(cameraLens).build();

        new ViewModelProvider(
            this,
            (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())
        )
            .get(CameraXViewModel.class)
            .getProcessCameraProvider()
            .observe( this, provider -> {
                cameraProvider = provider;
                bindCamera();
            });

        findViewById(R.id.btn_pause_scanning).setOnClickListener(v -> {
            if ( scanning_is_paused ) {
                bindCamera();
                ((Button) v).setText(R.string.scan_capture_bound);
            } else {
                cameraProvider.unbindAll();
                ((Button) v).setText(R.string.scan_capture_unbound);
            }

            scanning_is_paused = !scanning_is_paused;
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if ( barcodeProcessor != null )
            barcodeProcessor.stop();

        if ( cameraProvider != null )
            cameraProvider.unbindAll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if ( barcodeProcessor != null )
            barcodeProcessor.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindCamera();
    }

    private void bindCamera() {
        if ( cameraProvider != null ) {
            cameraProvider.unbindAll();
            bindCameraPreview();
            bindCameraAnalysis();

            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        }
    }

    private void bindCameraPreview() {
        if ( !PreferenceUtils.isLivePreviewEnabled(this) )
            return;

        if ( cameraProvider == null )
            return;

        if ( preview != null )
            cameraProvider.unbind(preview);

        Preview.Builder previewBuilder = new Preview.Builder();

        // get resolution from preferences
        ResolutionSelector res = PreferenceUtils.getTargetCameraResolution(this);
        if ( res != null )
            previewBuilder.setResolutionSelector( res );

        preview = previewBuilder.build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
    }

    // @OptIn because of `barcodeProcessor.processImageProxy`
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void bindCameraAnalysis() {
        if ( cameraProvider == null )
            return;

        if ( imageAnalysis != null )
            cameraProvider.unbind(imageAnalysis);

        if ( barcodeProcessor != null )
            barcodeProcessor.stop();

        barcodeProcessor = new BarcodeScannerProcessor(this);

        ImageAnalysis.Builder imageAnalysisBuilder = new ImageAnalysis.Builder();

        // get resolution from preferences
        ResolutionSelector res = PreferenceUtils.getTargetCameraResolution(this);
        if ( res != null )
            imageAnalysisBuilder.setResolutionSelector( res );

//        imageAnalysisBuilder.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST);
        imageAnalysis = imageAnalysisBuilder.build();

        needUpdateGraphicOverlayImageSourceInfo = true;
        imageAnalysis.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(this),
            imageProxy -> {
                if (needUpdateGraphicOverlayImageSourceInfo) {
                    boolean isImageFlipped = false;

                    int width  = imageProxy.getWidth();
                    int height = imageProxy.getHeight();
                    switch (imageProxy.getImageInfo().getRotationDegrees()) {
                        case 0:
                        case 180:
                            graphicOverlay.setImageSourceInfo(width, height, isImageFlipped);
                            break;
                        default:
                            //noinspection SuspiciousNameCombination
                            graphicOverlay.setImageSourceInfo(height, width, isImageFlipped);
                    }

                    needUpdateGraphicOverlayImageSourceInfo = false;
                }

                barcodeProcessor.processImageProxy(imageProxy, graphicOverlay);
            });
    }

    private boolean cameraPermissionsNotGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, CAMERA_REQUEST_CODE);
    }

    private static class CameraAccessErrorFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            builder
                .setTitle("Material Info")
                .setMessage("The scanner requires camera permissions to operate")
                .setCancelable(false)
                .setPositiveButton("Re-authorize", (dialog, id) -> {})
                .setNegativeButton("Go Back", (dialog, id) -> ((ScanActivity) getActivity()).onBackButtonClicked());

            return builder.create();
        }
    }
}