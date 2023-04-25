package edu.psu.pjm6196.inventorymanager;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

    private String[] camera_permissions;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

//        camera_permissions = new String[] {
//                Manifest.permission.CAMERA,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//        };

        findViewById(R.id.btn_scan).setOnClickListener(v -> {
//            new BarcodeAnalyzer().analyze();

        });


    }

    public void scanBarcode(InputImage image) {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_PDF417
                )
                .build();

        BarcodeScanner scanner = BarcodeScanning.getClient();
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
                            Toast.makeText(ScanActivity.this, "Scanned: " + barcode.getRawValue(), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ScanActivity.this, "unmatched barcode type: " + barcode.getValueType(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("ScanActivity::scanBarcode", e.toString());
                    Toast.makeText(ScanActivity.this, "Scanning failed...", Toast.LENGTH_SHORT).show();
                }
            });
    }


    public static class BarcodeAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        @OptIn(markerClass = ExperimentalGetImage.class)
        public void analyze(@NonNull ImageProxy image) {
            Image img = image.getImage();

            if (img != null) {
                InputImage input_img =
                    InputImage.fromMediaImage(img, image.getImageInfo().getRotationDegrees());
            }

            ImageAnalysis analysis = new ImageAnalysis.Builder()
//                    .setTargetResolution(new Size(1280, 720))
                    .build();
        }
    }
}