package edu.psu.pjm6196.inventorymanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class TakeInventoryActivity extends ScanActivityBase {
    public static final String TAG = "TakeInventory";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set action button icon
        ((FloatingActionButton) findViewById(R.id.btn_scanning_action))
            .setImageResource(R.drawable.ic_done);
    }

    @Override
    public void onClick(View view) {
        ArrayList<String> ids = (ArrayList<String>) barcodeProcessor.getScannedBarcodeIds();

        Log.d(TAG, "got result: " + ids);

        new AlertDialog.Builder(this)
            .setTitle("Barcodes Scanned")
            .setMessage(String.join("\n", ids))
            .setPositiveButton("Ok", (dialog, i) -> {
                Intent intent = new Intent(this, BarcodesListActivity.class);
                intent.putStringArrayListExtra("barcode_ids", ids);
                startActivity(intent);
            })
            .create()
            .show();
    }
}
