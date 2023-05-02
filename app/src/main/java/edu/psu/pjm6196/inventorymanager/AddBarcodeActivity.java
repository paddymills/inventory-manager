package edu.psu.pjm6196.inventorymanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import edu.psu.pjm6196.inventorymanager.db.Barcode;
import edu.psu.pjm6196.inventorymanager.db.BarcodeDatabase;
import edu.psu.pjm6196.inventorymanager.db.Material;

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

        findViewById(R.id.btn_submit).setOnClickListener(v -> {
            // find views
            TextView id_view    = findViewById(R.id.barcode_id);
            EditText mm_view    = findViewById(R.id.txt_mm);
            EditText grade_view = findViewById(R.id.txt_grade);
            EditText loc_view   = findViewById(R.id.txt_loc);
            EditText heat_view  = findViewById(R.id.txt_heat);
            EditText po_view    = findViewById(R.id.txt_po);

            // get values
            String id    = id_view.getText().toString();
            String mm    = mm_view.getText().toString();
            String grade = grade_view.getText().toString();
            String loc   = loc_view.getText().toString();
            String heat  = heat_view.getText().toString();
            String po    = po_view.getText().toString();

            // validate input
            if ( id.isEmpty() ) {
                Toast.makeText(this, "Barcode ID cannot be empty", Toast.LENGTH_SHORT).show();
                id_view.requestFocus();
            } else if ( mm.isEmpty() ) {
                Toast.makeText(this, "Material Master cannot be empty", Toast.LENGTH_SHORT).show();
                mm_view.requestFocus();
            } else if ( grade.isEmpty() ) {
                Toast.makeText(this, "Grade cannot be empty", Toast.LENGTH_SHORT).show();
                grade_view.requestFocus();
            } else if ( loc.isEmpty() ) {
                Toast.makeText(this, "Location cannot be empty", Toast.LENGTH_SHORT).show();
                loc_view.requestFocus();
            } else if ( heat.isEmpty() ) {
                Toast.makeText(this, "Heat Number cannot be empty", Toast.LENGTH_SHORT).show();
                heat_view.requestFocus();
            } else if ( po.isEmpty() ) {
                Toast.makeText(this, "PO Number cannot be empty", Toast.LENGTH_SHORT).show();
                po_view.requestFocus();
            } else {
                Barcode newBarcode = new Barcode(0, id, new Material(mm, loc, grade, heat, po));
                BarcodeDatabase.getDatabase(this).barcodeDAO().insert(newBarcode);

                Toast.makeText(this, "Barcode added", Toast.LENGTH_SHORT).show();

                // clear form
                id_view.setText("");
                mm_view.setText("");
                grade_view.setText("");
                loc_view.setText("");
                heat_view.setText("");
                po_view.setText("");
            }
        });
    }

    @Override
    protected void onBackButtonClicked() {
        startActivity(new Intent(this, BarcodesListActivity.class));
    }
}