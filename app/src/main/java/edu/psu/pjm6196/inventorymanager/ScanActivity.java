package edu.psu.pjm6196.inventorymanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.Toolbar;
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

import java.util.ArrayList;

import edu.psu.pjm6196.inventorymanager.barcodescanner.BarcodeScannerProcessor;
import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.GraphicOverlay;
import edu.psu.pjm6196.inventorymanager.barcodescanner.utils.CameraXViewModel;
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
        ADD_MATERIAL("add"),
        FIND_MATERIAL("find"),
        MOVE_MATERIAL("move"),

        // multiple barcodes
        FILTER_LIST("filter");
//        TAKE_INVENTORY("take_inventory");


        private final String name;
        CallingActivityIntent(String name) {
            this.name = name;
        }

        // enum constructor from: https://stackoverflow.com/a/2965252
        public static CallingActivityIntent fromString(String name) {
            for (CallingActivityIntent c : CallingActivityIntent.values() ) {
                if ( c.name.equalsIgnoreCase(name) )
                    return c;
            }

            return null;
        }

        public boolean isSingleBarcodeScanUseCase() {
            switch (this) {
                case ADD_MATERIAL:
                case FIND_MATERIAL:
                case MOVE_MATERIAL:
                    return true;
            }

            return false;
        }

        @NonNull
        @Override
        public String toString() {
            return this.name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Log.i(TAG, "onCreate");

        Intent callingIntent = getIntent();
        String use_case = callingIntent.getStringExtra("calling_activity_intent");
        if ( use_case != null )
            scan_use_case = CallingActivityIntent.fromString(use_case);

        // set barcode scan lifetime
        BarcodeScannerProcessor.set_barcode_lifetime(PreferenceUtils.getBarcodeLifetime(this));

        // request camera permissions
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle instanceState) {
        super.onSaveInstanceState(instanceState);

        instanceState.putString("calling_activity_intent", scan_use_case.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle instanceState) {
        super.onRestoreInstanceState(instanceState);

        String use_case = instanceState.getString("calling_activity_intent");
        if ( use_case == null )
            Log.e(TAG, "got null use case");
        else
            scan_use_case = CallingActivityIntent.fromString(use_case);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scan_menu, menu);

        menu.findItem(R.id.menu_submit).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_submit) {
            return returnResult();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getToolbarTitleResId() {
        return R.string.scan_title;
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

//    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // to handle touch event on GraphicOverlay
        if ( motionEvent.getAction() == MotionEvent.ACTION_DOWN ) {

            ArrayList<String> touchedBarcodes = barcodeProcessor.handleTouchEvent(motionEvent);
            if ( touchedBarcodes.size() > 0 ) {
                // touch occurred in at least 1 barcode
                // probably will only ever happen in 1 barcode (until we impl swiping to select),
                // but we should just handle multiples in case

                if (
                    this.scan_use_case.isSingleBarcodeScanUseCase() &&
                    // use >= here just in case something bad happened and selected barcodes is > 1
                    barcodeProcessor.getToBeNumberOfBarcodesSelected(touchedBarcodes) >= 1
                ) {
                    Toast.makeText(this, "Cannot select multiple barcodes for this use case", Toast.LENGTH_SHORT).show();
                    barcodeProcessor.clearSelected();

                    // remove all but the first barcode
                    touchedBarcodes.subList(1, touchedBarcodes.size()).clear();
                }

                barcodeProcessor.commitBarcodeTouchEvents(touchedBarcodes);
                Toolbar menu = findViewById(R.id.toolbar);
                if ( barcodeProcessor.getNumberOfBarcodesSelected() == 0 ) {
                    Log.d(TAG, "No barcodes selected anymore");
                    menu.getMenu().findItem(R.id.menu_submit).setVisible(false);
                } else {
                    Log.d(TAG, "Barcode(s) touched: " + touchedBarcodes.toString());
                    menu.getMenu().findItem(R.id.menu_submit).setVisible(true);
                }

                view.performClick();
                return true;
            }
        }

        view.performClick();
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
        return returnToCallingActivity(intent -> {
            if ( scan_use_case.isSingleBarcodeScanUseCase() )
                intent.putExtra("barcode_id", barcodeProcessor.getSelectedBarcodeId());
            else
                intent.putStringArrayListExtra("barcode_ids", (ArrayList<String>) barcodeProcessor.getSelectedBarcodeIds());
        });
    }
}