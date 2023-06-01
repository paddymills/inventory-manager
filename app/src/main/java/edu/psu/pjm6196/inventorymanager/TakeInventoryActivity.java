package edu.psu.pjm6196.inventorymanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class TakeInventoryActivity extends ScanActivityBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set action button icon
        ((FloatingActionButton) findViewById(R.id.btn_scanning_action))
            .setImageResource(R.drawable.ic_done);

//        findViewById(R.id.btn_pause_scanning).setOnClickListener(this);

        new AlertDialog.Builder(this)
            .setTitle("Barcodes Scanned")
            .setMessage("Hello from the TakeInventoryActivity")
            .setPositiveButton("Ok",
                (dialog, i) -> dialog.dismiss())
            .create()
            .show();
    }

    @Override
    public void onClick(View view) {
        returnToCallingActivity(intent -> {
            intent.putStringArrayListExtra("barcode_ids", (ArrayList<String>) barcodeProcessor.getScannedBarcodeIds());
        });
    }
}
