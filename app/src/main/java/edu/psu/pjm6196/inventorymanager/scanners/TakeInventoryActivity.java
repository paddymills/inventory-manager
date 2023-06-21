package edu.psu.pjm6196.inventorymanager.scanners;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import edu.psu.pjm6196.inventorymanager.BarcodesListActivity;
import edu.psu.pjm6196.inventorymanager.R;

public class TakeInventoryActivity extends BaseScanActivity {
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
        ArrayList<String> ids = (ArrayList<String>) barcodes.getScannedBarcodeIds();

        Log.d(TAG, "Scanned barcodes: " + ids);

        Intent intent = new Intent(this, BarcodesListActivity.class);
        intent.putStringArrayListExtra("barcode_ids", ids);

//        finish();
        startActivity(intent);
    }
}
