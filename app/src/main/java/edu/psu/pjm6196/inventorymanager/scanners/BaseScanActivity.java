package edu.psu.pjm6196.inventorymanager.scanners;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.ArrayList;
import java.util.List;

import edu.psu.pjm6196.inventorymanager.ActivityBase;
import edu.psu.pjm6196.inventorymanager.R;
import edu.psu.pjm6196.inventorymanager.barcodescanner.BarcodeCollection;
import edu.psu.pjm6196.inventorymanager.barcodescanner.GraphicOverlay;
import edu.psu.pjm6196.inventorymanager.barcodescanner.ScannedBarcodeHandler;
import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.BarcodeGraphic;
import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.CameraImageGraphic;
import edu.psu.pjm6196.inventorymanager.barcodescanner.graphics.TelemetryInfoGraphic;
import edu.psu.pjm6196.inventorymanager.barcodescanner.processor.BarcodeScannerProcessor;
import edu.psu.pjm6196.inventorymanager.barcodescanner.utils.CameraXViewModel;
import edu.psu.pjm6196.inventorymanager.utils.PreferenceUtils;

public abstract class BaseScanActivity
    extends ActivityBase
    implements View.OnTouchListener, View.OnClickListener, ScannedBarcodeHandler {

    public static final int cameraLens = CameraSelector.LENS_FACING_BACK;
    /*
        This class and its supporting classes/utilities are either copied from (where noted)
        or heavily influenced by the design of Google's sample codebase
        at https://github.com/googlesamples/mlkit/tree/master/android/vision-quickstart
     */
    protected static final String TAG = "ScanActivityBase";
    protected static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    protected static final int CAMERA_REQUEST_CODE = 475;
    protected BarcodeScannerProcessor barcodeProcessor;
    protected ProcessCameraProvider cameraProvider;
    protected CameraSelector cameraSelector;
    protected Preview preview;
    protected ImageAnalysis imageAnalysis;
    protected BarcodeCollection barcodes;
    protected boolean requiresImageSourceUpdate;
    protected boolean scanningIsPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // request camera permissions
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, CAMERA_REQUEST_CODE);

        barcodes = new BarcodeCollection();

        findViewById(R.id.btn_scanning_action).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "resumed...");

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scan_menu, menu);

        menu.findItem(R.id.menu_submit).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_submit)
            return returnResult();

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

    // @Override if only 1 barcode is expected (i.e. AddBarcode)
    protected boolean isSingleBarcodeScanUseCase() {
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // to handle touch event on GraphicOverlay
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

            ArrayList<String> touchedBarcodes = barcodes.handleTouchEvent(motionEvent);
            if (touchedBarcodes.size() > 0) {
                // touch occurred in at least 1 barcode
                // probably will only ever happen in 1 barcode (until we impl swiping to select),
                // but we should just handle multiples in case

                if (
                    this.isSingleBarcodeScanUseCase() &&
                        // use >= here just in case something bad happened and selected barcodes is > 1
                        barcodes.getToBeNumberOfBarcodesSelected(touchedBarcodes) > 1
                ) {
                    Toast.makeText(this, "Cannot select multiple barcodes for this use case", Toast.LENGTH_SHORT).show();
                    barcodes.clearSelected();

                    // remove all but the first barcode
                    touchedBarcodes.subList(1, touchedBarcodes.size()).clear();
                }

                // valid touch scenario -> commit changes
                barcodes.commitBarcodeTouchEvents(touchedBarcodes);
                Toolbar menu = findViewById(R.id.toolbar);
                if (barcodes.getNumberOfBarcodesSelected() == 0) {
                    Log.d(TAG, "No barcodes selected anymore");
                    menu.getMenu().findItem(R.id.menu_submit).setVisible(false);
                } else {
                    Log.d(TAG, "Barcode(s) touched: " + touchedBarcodes);
                    menu.getMenu().findItem(R.id.menu_submit).setVisible(true);

                    // call for overlay to re-paint
                    findViewById(R.id.scan_overlay).postInvalidate();
                }
            }

            view.performClick();
            return true;
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        FloatingActionButton btn = (FloatingActionButton) view;
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

        if (barcodeProcessor != null)
            barcodeProcessor.stop();
        barcodeProcessor = new BarcodeScannerProcessor(this, this);

        GraphicOverlay graphicOverlay = findViewById(R.id.scan_overlay);
        graphicOverlay.setDataListener(this);

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
            // so we can just runs the analyzer itself on main thread.
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

    @Override
    public void imageAnalyzedWithBarcodes(List<Barcode> barcodeList, @NonNull Bitmap image) {
        GraphicOverlay overlay = findViewById(R.id.scan_overlay);
        overlay.updateImage(new CameraImageGraphic(image));
        barcodeList.forEach(barcode -> barcodes.addBarcode(barcode));
    }

    @Override
    public void updateTelemetry(long frameLatency, long detectorLatency, int framesPerSecond) {
        getOverlay()
            .updateTelemetry(
                new TelemetryInfoGraphic(frameLatency, detectorLatency, framesPerSecond)
            );
    }

    @Override
    public GraphicOverlay getOverlay() {
        return findViewById(R.id.scan_overlay);
    }

    @Override
    public List<BarcodeGraphic> getBarcodesToRender() {
        return barcodes.getBarcodesInView();
    }

    protected boolean returnResult() {
        return returnToCallingActivity(intent -> {
            intent.putStringArrayListExtra("barcode_ids", (ArrayList<String>) barcodes.getSelectedBarcodeIds());
        });
    }
}