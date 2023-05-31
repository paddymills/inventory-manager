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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import edu.psu.pjm6196.inventorymanager.barcodescanner.BarcodeScannerProcessor;
import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.GraphicOverlay;
import edu.psu.pjm6196.inventorymanager.barcodescanner.utils.CameraXViewModel;
import edu.psu.pjm6196.inventorymanager.utils.PreferenceUtils;

public class ScanActivity extends CustomAppCompatActivity implements View.OnTouchListener {

    public static final int cameraLens = CameraSelector.LENS_FACING_BACK;
    /*
        This class and its supporting classes/utilities are either copied from (where noted)
        or heavily influenced by the design of Google's sample codebase
        at https://github.com/googlesamples/mlkit/tree/master/android/vision-quickstart
     */
    private static final String TAG = "ScanActivity";
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 475;
    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private Preview preview;
    private ImageAnalysis imageAnalysis;
    private BarcodeScannerProcessor barcodeProcessor;
    private boolean requiresImageSourceUpdate;
    private boolean scanningIsPaused;
    private int scanUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Intent callingIntent = getIntent();
        scanUseCase = callingIntent.getIntExtra("calling_activity_intent", 0);

        // request camera permissions
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, CAMERA_REQUEST_CODE);

        findViewById(R.id.btn_pause_scanning).setOnClickListener(v -> {
            FloatingActionButton btn = (FloatingActionButton) v;
            if (scanningIsPaused) {
                bindCamera();
                btn.setContentDescription(getResources().getString(R.string.scan_capture_bound));
//                btn.setImageResource(R.drawable.ic_pause);
            } else {
                cameraProvider.unbindAll();
                btn.setContentDescription(getResources().getString(R.string.scan_capture_unbound));
//                btn.setImageResource(R.drawable.ic_play);
            }

            btn.setImageResource(scanningIsPaused ? R.drawable.ic_play : R.drawable.ic_pause);
            scanningIsPaused = !scanningIsPaused;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "resumed...");

        // set barcode scan lifetime
        BarcodeScannerProcessor.setBarcodeLifetime(PreferenceUtils.getBarcodeLifetime(this));

        bindCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (barcodeProcessor != null)
            barcodeProcessor.stop();

        if (cameraProvider != null)
            cameraProvider.unbindAll();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle instanceState) {
        super.onSaveInstanceState(instanceState);

        instanceState.putInt("calling_activity_intent", scanUseCase);
    }

    @Override
    protected void onRestoreInstanceState(Bundle instanceState) {
        super.onRestoreInstanceState(instanceState);

        scanUseCase = instanceState.getInt("calling_activity_intent");
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

            // if we need to dynamically select camera lens use CameraSelector.Builder
            cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cameraLens)
                .build();

            new ViewModelProvider(
                this,
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory
                    .getInstance(getApplication())
            )
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(this, provider -> {
                    cameraProvider = provider;
                    bindCamera();
                });
        } else {
            Toast.makeText(this, "Barcode scanner requires camera permissions", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // to handle touch event on GraphicOverlay
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

            ArrayList<String> touchedBarcodes = barcodeProcessor.handleTouchEvent(motionEvent);
            if (touchedBarcodes.size() > 0) {
                // touch occurred in at least 1 barcode
                // probably will only ever happen in 1 barcode (until we impl swiping to select),
                // but we should just handle multiples in case

                if (
                    CallingActivityIntent.isSingleBarcodeScanUseCase(this.scanUseCase) &&
                        // use >= here just in case something bad happened and selected barcodes is > 1
                        barcodeProcessor.getToBeNumberOfBarcodesSelected(touchedBarcodes) > 1
                ) {
                    Toast.makeText(this, "Cannot select multiple barcodes for this use case", Toast.LENGTH_SHORT).show();
                    barcodeProcessor.clearSelected();

                    // remove all but the first barcode
                    touchedBarcodes.subList(1, touchedBarcodes.size()).clear();
                }

                // valid touch scenario -> commit changes
                barcodeProcessor.commitBarcodeTouchEvents(touchedBarcodes);
                Toolbar menu = findViewById(R.id.toolbar);
                if (barcodeProcessor.getNumberOfBarcodesSelected() == 0) {
                    Log.d(TAG, "No barcodes selected anymore");
                    menu.getMenu().findItem(R.id.menu_submit).setVisible(false);
                } else {
                    Log.d(TAG, "Barcode(s) touched: " + touchedBarcodes);
                    menu.getMenu().findItem(R.id.menu_submit).setVisible(true);
                }

                barcodeProcessor.validateBarcodesDisplayed(findViewById(R.id.scan_overlay));
            }

            view.performClick();
            return true;
        }

        return false;
    }

    private void bindCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            bindCameraPreview();
            bindCameraAnalysis();

            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        }
    }

    private void bindCameraPreview() {
        if (!PreferenceUtils.isLivePreviewEnabled(this))
            return;

        if (cameraProvider == null)
            return;

        if (preview != null)
            cameraProvider.unbind(preview);

        Preview.Builder previewBuilder = new Preview.Builder();

        // get resolution from preferences
        ResolutionSelector res = PreferenceUtils.getTargetCameraResolution(this);
        if (res != null)
            previewBuilder.setResolutionSelector(res);

        preview = previewBuilder.build();
        preview.setSurfaceProvider(
            ((PreviewView) findViewById(R.id.scan_preview)).getSurfaceProvider()
        );
    }

    // @OptIn because of `barcodeProcessor.processImageProxy`
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void bindCameraAnalysis() {
        if (cameraProvider == null)
            return;

        if (imageAnalysis != null)
            cameraProvider.unbind(imageAnalysis);

        if (barcodeProcessor == null)
            barcodeProcessor = new BarcodeScannerProcessor(this);
        else {
            barcodeProcessor.stop();
            barcodeProcessor = new BarcodeScannerProcessor(this, barcodeProcessor);
        }

        GraphicOverlay graphicOverlay = findViewById(R.id.scan_overlay);

        ImageAnalysis.Builder imageAnalysisBuilder = new ImageAnalysis.Builder();

        // get resolution from preferences
        ResolutionSelector res = PreferenceUtils.getTargetCameraResolution(this);
        if (res != null)
            imageAnalysisBuilder.setResolutionSelector(res);

        imageAnalysisBuilder.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST);
        imageAnalysis = imageAnalysisBuilder.build();

        requiresImageSourceUpdate = true;
        imageAnalysis.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(this),
            imageProxy -> {
                if (requiresImageSourceUpdate) {
                    boolean isImageFlipped = false;

                    int width = imageProxy.getWidth();
                    int height = imageProxy.getHeight();
                    switch (imageProxy.getImageInfo().getRotationDegrees()) {
                        case 0, 180 ->
                            graphicOverlay.setImageSourceInfo(width, height, isImageFlipped);
                        default ->
                            //noinspection SuspiciousNameCombination
                            graphicOverlay.setImageSourceInfo(height, width, isImageFlipped);
                    }

                    requiresImageSourceUpdate = false;
                }

                barcodeProcessor.processImageProxy(imageProxy, graphicOverlay);
            });


        graphicOverlay.setOnTouchListener(this);
    }

    private boolean returnResult() {
        return returnToCallingActivity(intent -> {
            // TODO: refactor to return ArrayList<String>
            if (CallingActivityIntent.isSingleBarcodeScanUseCase(scanUseCase))
                intent.putExtra("barcode_id", barcodeProcessor.getSelectedBarcodeId());
            else
                intent.putStringArrayListExtra("barcode_ids", (ArrayList<String>) barcodeProcessor.getSelectedBarcodeIds());
        });
    }

    // for knowing what the calling activity wants to do with the scanned data barcode(s)
    public static class CallingActivityIntent {
        private static int enumCounter = 1;
        // single barcode
        public static final int ADD_MATERIAL = enumCounter++;
        public static final int MOVE_MATERIAL = enumCounter++;

        // divider
        private static final int SINGLE_MULTI_USE_CASE_SPLIT = enumCounter++;

        // multiple barcodes
        public static final int FIND_MATERIAL = enumCounter++;
        public static final int FILTER_LIST = enumCounter++;
//        public static final int TAKE_INVENTORY = 5;


        public static boolean isSingleBarcodeScanUseCase(int use_case) {
            return use_case < SINGLE_MULTI_USE_CASE_SPLIT;
        }
    }
}