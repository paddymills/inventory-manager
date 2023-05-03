package edu.psu.pjm6196.inventorymanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import edu.psu.pjm6196.inventorymanager.db.Barcode;
import edu.psu.pjm6196.inventorymanager.db.BarcodeDatabase;
import edu.psu.pjm6196.inventorymanager.db.Material;

public class AddBarcodeActivity extends CustomAppCompatActivity {

    private static final String TAG = "AddBarcode";
    public static final int ADD_MODE = 0;
    public static final int EDIT_MODE = 1;
    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_barcode);

        // handle data passed from other intents
        handleIntent(getIntent());

        // TODO: (after final) impl location selector using spinner
        findViewById(R.id.btn_change_loc).setVisibility(View.GONE);

        findViewById(R.id.btn_set_barcode)
            .setOnClickListener(this::scanBarcodeButtonClicked);

        findViewById(R.id.btn_submit)
            .setOnClickListener(this::submitButtonClicked);
    }

    @Override
    protected int getToolbarTitleResId() {
        if ( mode == ADD_MODE )
            return R.string.add_title;
        else    // edit mode
            return R.string.edit_title;
    }

    @Override
    protected Class<?> getBackButtonClass() {
        // TODO: allow this to fired from MainActivity
        return BarcodesListActivity.class;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle instanceState) {
        super.onSaveInstanceState(instanceState);
        instanceState.putInt("mode", mode);

        instanceState.putParcelable("barcode", getFormValuesUnchecked(false));

    }

    @Override
    protected void onRestoreInstanceState(Bundle instanceState) {
        super.onRestoreInstanceState(instanceState);

        this.mode = instanceState.getInt("mode", ADD_MODE);

        setFormValues( instanceState.getParcelable("barcode") );
    }

    private void handleIntent(Intent intent) {
        if ( intent.hasExtra("mode") )
            this.mode = intent.getIntExtra("mode", ADD_MODE);

        // TODO: edit mode
        if ( intent.hasExtra("barcode_id") ) {
            // activity started from scanner (scanner has result)
            String scanned_barcode = intent.getStringExtra("barcode");
            Log.d(TAG, "Got barcode from scanner: " + scanned_barcode);

            if ( BarcodeDatabase
                    .getDatabase(this)
                    .barcodeDAO()
                    .getByIdHash(scanned_barcode) != null ) {
                new AlertDialog.Builder(this)
                    .setMessage("Barcode id `" + scanned_barcode + "` already exists in the database")
                    .setPositiveButton("Scan a different barcode", (dialog, id) -> scanBarcodeButtonClicked(null))
                    .setNegativeButton("Cancel and exit Add/Edit mode", (dialog, id) -> onBackButtonClicked(null))
                    .create()
                    .show();
            }

            ((TextView) findViewById(R.id.barcode_id)).setText(scanned_barcode);
        }

        if ( mode == EDIT_MODE) {
            Barcode barcode = (Barcode) intent.getParcelableExtra("barcode");
            setFormValues(barcode);
        }
    }

    private void scanBarcodeButtonClicked(View view) {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra("calling_activity", "AddBarcode");
        intent.putExtra("calling_activity_intent", ScanActivity.CallingActivityIntent.ADD_MATERIAL.toString());

        startActivity(intent);
    }

    private void submitButtonClicked(View view) {
        Barcode barcode = getFormValuesChecked();
        if ( barcode != null ) {
            // TODO: validate barcode does not exist if in database


            if ( mode == ADD_MODE ) {
                BarcodeDatabase.insert(barcode);
                Toast.makeText(this, "Barcode added", Toast.LENGTH_SHORT).show();
            }
            else {    // edit mode
                BarcodeDatabase.getBarcodeByIdHash(
                    barcode.id_hash,
                    b -> {
                        barcode.id = b.id;

                        BarcodeDatabase.update(barcode);
                        Toast.makeText(this, "Barcode updated", Toast.LENGTH_SHORT).show();
                    }
                );

            }

            // clear form
//            setFormValues(null);
            onBackButtonClicked(view);
        }

    }

    private void setFormValues(Barcode barcode) {
        // find views
        TextView id_view    = findViewById(R.id.barcode_id);
        EditText mm_view    = findViewById(R.id.txt_mm);
        EditText grade_view = findViewById(R.id.txt_grade);
        EditText loc_view   = findViewById(R.id.txt_loc);
        EditText heat_view  = findViewById(R.id.txt_heat);
        EditText po_view    = findViewById(R.id.txt_po);

        id_view.setText(   barcode == null ? "" : barcode.id_hash);
        mm_view.setText(   barcode == null ? "" : barcode.material.material_master);
        grade_view.setText(barcode == null ? "" : barcode.material.grade);
        loc_view.setText(  barcode == null ? "" : barcode.material.location);
        heat_view.setText( barcode == null ? "" : barcode.material.heat_number);
        po_view.setText(   barcode == null ? "" : barcode.material.po_number);
    }

    private Barcode getFormValues() {
        Barcode barcode = getFormValuesUnchecked(false);
        if (
            barcode.id_hash.equals("") ||
            barcode.material.material_master.equals("") ||
            barcode.material.grade.equals("") ||
            barcode.material.location.equals("") ||
            barcode.material.heat_number.equals("") ||
            barcode.material.po_number.equals("")
        )
            return null;

        return barcode;
    }

    private Barcode getFormValuesChecked() {
        Barcode barcode = getFormValuesUnchecked(true);
        if (
            barcode.id_hash.equals("") ||
                barcode.material.material_master.equals("") ||
                barcode.material.grade.equals("") ||
                barcode.material.location.equals("") ||
                barcode.material.heat_number.equals("") ||
                barcode.material.po_number.equals("")
        )
            return null;

        return barcode;
    }

    private Barcode getFormValuesUnchecked(boolean alert) {
        // get values
        String id    = validateTextView(R.id.barcode_id, "Barcode ID", alert);
        String mm    = validateTextView(R.id.txt_mm,"Material Master", alert);
        String grade = validateTextView(R.id.txt_grade, "Material Grade", alert);
        String loc   = validateTextView(R.id.txt_loc, "Location", alert);
        String heat  = validateTextView(R.id.txt_heat, "Heat Number", alert);
        String po    = validateTextView(R.id.txt_po, "PO Number", alert);

        return new Barcode(0, id, new Material(mm, loc, grade, heat, po));
    }

    /**
     * @param id resource ID of the TextView
     * @param title TextView title for toasting
     * @param alertOnError toast if there is a validation error
     * @return Value of TextView if valid, otherwise null
     */
    private String validateTextView(int id, String title, boolean alertOnError) {
        // validates that the text view is not empty
        // returns null if empty, otherwise

        String val = ((TextView) findViewById(id)).getText().toString();

        if ( val.isEmpty() ) {
            if ( alertOnError ) {
                Toast.makeText(this, title + " cannot be empty", Toast.LENGTH_SHORT).show();
                findViewById(id).requestFocus();
            }

            return null;
        }

        return val;
    }
}