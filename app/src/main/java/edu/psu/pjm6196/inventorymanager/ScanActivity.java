package edu.psu.pjm6196.inventorymanager;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class ScanActivity extends CustomAppCompatActivity {

    private String TAG = "ScanActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        findViewById(R.id.btn_scan).setOnClickListener(v -> scanBarcode());
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
                                    Log.e(TAG, "Scanned barcode: " + rawValue);
                                    Toast.makeText(ScanActivity.this, rawValue, Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Failed to process barcode...");
                            Toast.makeText(ScanActivity.this, "Failed to process barcode.", Toast.LENGTH_LONG).show();
                        }
                    });
                // Pass image to an ML Kit Vision API
                // ...
            }

        }
    }
}