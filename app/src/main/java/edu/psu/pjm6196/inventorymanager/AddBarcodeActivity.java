package edu.psu.pjm6196.inventorymanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AddBarcodeActivity extends CustomAppCompatActivity {

    private static final String TAG = "AddBarcode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_barcode);

        Intent callingIntent = getIntent();
        if ( callingIntent.hasExtra("barcode") ) {
            String scanned_barcode = callingIntent.getStringExtra("barcode");
            Log.d(TAG, "Got barcode from scanner: " + scanned_barcode);

            // TODO: check if ID is already in database
            ((TextView) findViewById(R.id.barcode_id)).setText(scanned_barcode);
        }

        // TODO: impl location selector using spinner
        findViewById(R.id.btn_change_loc).setVisibility(View.GONE);

        findViewById(R.id.btn_set_barcode)
            .setOnClickListener(v -> {
                Intent intent = new Intent(this, ScanActivity.class);
                intent.putExtra("calling_activity_intent", ScanActivity.CallingActivityIntent.AddMaterial);

                startActivity(intent);
            });
    }

    @Override

    protected void onBackButtonClicked() {
        startActivity(new Intent(this, BarcodesListActivity.class));
    }
}