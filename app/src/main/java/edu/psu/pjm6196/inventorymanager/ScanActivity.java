package edu.psu.pjm6196.inventorymanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import androidx.lifecycle.ViewModelProvider;

import edu.psu.pjm6196.inventorymanager.barcodescanner.BarcodeScannerProcessor;
import edu.psu.pjm6196.inventorymanager.barcodescanner.utils.CameraXViewModel;
import edu.psu.pjm6196.inventorymanager.barcodescanner.utils.GraphicOverlay;
import edu.psu.pjm6196.inventorymanager.utils.PreferenceUtils;

public class ScanActivity extends CustomAppCompatActivity implements View.OnTouchListener {

    /*
        This class and its supporting classes/utilities are either copied from (where noted)
        or heavily influenced by the design of Google's sample codebase
        at https://github.com/googlesamples/mlkit/tree/master/android/vision-quickstart
     */
    private static final String TAG = "ScanActivity";

    private static final String[] CAMERA_PERMISSION = new String[] {Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 475;

    public static final int cameraLens = CameraSelector.LENS_FACING_BACK;
    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;
    private Menu toolbar;

    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private Preview preview;
    private ImageAnalysis imageAnalysis;
    private BarcodeScannerProcessor barcodeProcessor;
    private boolean requiresImageSourceUpdate;
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

        public boolean isSingleBarcodeScanUseCase() {
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

        // request camera permissions
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, CAMERA_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scan_menu, menu);

        menu.getItem(0).setVisible(false);
        toolbar = menu;

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (
            requestCode == CAMERA_REQUEST_CODE &&
            grantResults.length > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            postPermissionsGrantedSetup();
        }

        else {
            Toast.makeText(this, "Barcode scanner requires camera permissions", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void postPermissionsGrantedSetup() {
        previewView = findViewById(R.id.scan_preview);
        graphicOverlay = findViewById(R.id.scan_overlay);

        graphicOverlay.setOnTouchListener(this);

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
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // to handle touch event on GraphicOverlay
        if ( motionEvent.getAction() == MotionEvent.ACTION_DOWN ) {

            float x = motionEvent.getX();
            float y = motionEvent.getY();
            if ( barcodeProcessor.handleTouchEvent((int) x, (int) y) ) {
                // touch occurred in at least 1 barcode
                long numSelected = barcodeProcessor.getNumberOfBarcodesSelected();
                if ( numSelected == 0 ) {
                    Log.d(TAG, "No barcodes selected");
                    toolbar.getItem(0).setVisible(false);
                } else if ( numSelected > 1 && this.scan_use_case.isSingleBarcodeScanUseCase() ) {
                    Toast.makeText(this, "Cannot select multiple barcodes for this use case", Toast.LENGTH_SHORT).show();
                    toolbar.getItem(0).setVisible(false);
                } else {
                    Log.d(TAG, "Barcodes selected");
                    toolbar.getItem(0)
                        .setEnabled(true)
                        .setOnMenuItemClickListener(item -> returnResult());
                }

                return true;
            }
        }

        return false;
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

        requiresImageSourceUpdate = true;
        imageAnalysis.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(this),
            imageProxy -> {
                if (requiresImageSourceUpdate) {
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

                    requiresImageSourceUpdate = false;
                }

                barcodeProcessor.processImageProxy(imageProxy, graphicOverlay);
            });
    }

    private boolean returnResult() {
        Intent intent = new Intent(this, getCallingActivity().getClass());

        if ( scan_use_case.isSingleBarcodeScanUseCase() )
            intent.putExtra("barcode", barcodeProcessor.getSelectedBarcodeId());
        else
            intent.putExtra("barcodes", barcodeProcessor.getSelectedBarcodeIds());

        return true;
    }
}