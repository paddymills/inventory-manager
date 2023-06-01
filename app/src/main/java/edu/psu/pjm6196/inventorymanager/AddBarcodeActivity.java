package edu.psu.pjm6196.inventorymanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import edu.psu.pjm6196.inventorymanager.db.Barcode;
import edu.psu.pjm6196.inventorymanager.db.BarcodeDatabase;
import edu.psu.pjm6196.inventorymanager.db.Material;

public class AddBarcodeActivity extends ActivityBase {

    public static final int ADD_MODE = 0;
    public static final int EDIT_MODE = 1;
    private static final String TAG = "AddBarcode";
    private int mode;
    private Barcode barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_barcode);

        // handle data passed from other intents
        Intent intent = getIntent();
        if (intent.hasExtra("mode"))
            this.mode = intent.getIntExtra("mode", ADD_MODE);

        if (mode == EDIT_MODE) {
            barcode = intent.getParcelableExtra("barcode");
            setFormValues();
        }

        ActivityResultLauncher<Intent> getBarcodeId = registerForActivityResult(
            new ActivityResultContract<Intent, String>() {
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, Intent intent) {
                    intent.putExtra("calling_activity_intent", ScanActivity.CallingActivityIntent.ADD_MATERIAL);
                    return intent;
                }

                @Override
                public String parseResult(int resultCode, @Nullable Intent intent) {
                    if (resultCode == RESULT_OK && intent != null)
                        return intent.getStringExtra("barcode_id");

                    return null;
                }
            },
            scannedBarcode -> {
                Log.d(TAG, "Barcode Id received: " + scannedBarcode);
                if (scannedBarcode != null) {
                    if (BarcodeDatabase
                        .getDatabase(this)
                        .barcodeDAO()
                        .getByIdHash(scannedBarcode) != null) {
                        new AlertDialog.Builder(this)
                            .setMessage("Barcode id `" + scannedBarcode + "` already exists in the database")
                            .setPositiveButton("Scan a different barcode",
                                (dialog, id) -> findViewById(R.id.btn_set_barcode).performClick())
                            .setNegativeButton("Cancel and exit Add/Edit mode", (dialog, id) -> {
                                setResult(RESULT_CANCELED);
                                finish();
                            })
                            .create()
                            .show();
                    }

                    ((TextView) findViewById(R.id.barcode_id)).setText(scannedBarcode);
                }
            });

        // TODO: (after final) impl location selector using spinner
        findViewById(R.id.btn_change_loc).setVisibility(View.GONE);

        findViewById(R.id.btn_set_barcode)
            .setOnClickListener(
                v -> getBarcodeId.launch(new Intent(this, ScanActivity.class)));

        findViewById(R.id.btn_submit)
            .setOnClickListener(v -> {
                if (getFormValuesChecked()) {
                    if (mode == ADD_MODE) {
                        // TODO: validate barcode does not exist if in database
                        BarcodeDatabase.insert(barcode);
                    } else {    // edit mode
                        BarcodeDatabase.update(barcode);
                    }

                    setResult(RESULT_OK);
                    finish();
                }

                // no need for an else because getFormValuesChecked() will
                //   toast the first form validation issue
            });
    }

    @Override
    protected int getToolbarTitleResId() {
        if (mode == ADD_MODE)
            return R.string.add_title;
        else    // edit mode
            return R.string.edit_title;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle instanceState) {
        super.onSaveInstanceState(instanceState);
        instanceState.putInt("mode", mode);

        getFormValuesUnchecked(false);
        instanceState.putParcelable("barcode", barcode);
    }

    @Override
    protected void onRestoreInstanceState(Bundle instanceState) {
        super.onRestoreInstanceState(instanceState);
        this.mode = instanceState.getInt("mode", ADD_MODE);

        barcode = instanceState.getParcelable("barcode");
        setFormValues();
    }

    private void setFormValues() {
        // find views
        TextView id_view = findViewById(R.id.barcode_id);
        EditText mm_view = findViewById(R.id.txt_mm);
        EditText grade_view = findViewById(R.id.txt_grade);
        EditText loc_view = findViewById(R.id.txt_loc);
        EditText heat_view = findViewById(R.id.txt_heat);
        EditText po_view = findViewById(R.id.txt_po);

        id_view.setText(barcode == null ? "" : barcode.id_hash);
        mm_view.setText(barcode == null ? "" : barcode.material.material_master);
        grade_view.setText(barcode == null ? "" : barcode.material.grade);
        loc_view.setText(barcode == null ? "" : barcode.material.location);
        heat_view.setText(barcode == null ? "" : barcode.material.heat_number);
        po_view.setText(barcode == null ? "" : barcode.material.po_number);
    }

    private boolean getFormValuesChecked() {
        getFormValuesUnchecked(true);
        return !barcode.id_hash.equals("") &&
            !barcode.material.material_master.equals("") &&
            !barcode.material.grade.equals("") &&
            !barcode.material.location.equals("") &&
            !barcode.material.heat_number.equals("") &&
            !barcode.material.po_number.equals("");
    }

    private void getFormValuesUnchecked(boolean alert) {
        // get values
        String id = validateTextView(R.id.barcode_id, "Barcode ID", alert);
        String mm = validateTextView(R.id.txt_mm, "Material Master", alert);
        String grade = validateTextView(R.id.txt_grade, "Material Grade", alert);
        String loc = validateTextView(R.id.txt_loc, "Location", alert);
        String heat = validateTextView(R.id.txt_heat, "Heat Number", alert);
        String po = validateTextView(R.id.txt_po, "PO Number", alert);

        barcode.id_hash = id;
        barcode.material = new Material(mm, loc, grade, heat, po);
    }

    /**
     * @param id           resource ID of the TextView
     * @param title        TextView title for toasting
     * @param alertOnError toast if there is a validation error
     * @return Value of TextView if valid, otherwise null
     */
    private String validateTextView(int id, String title, boolean alertOnError) {
        // validates that the text view is not empty
        // returns null if empty, otherwise

        String val = ((TextView) findViewById(id)).getText().toString();

        if (val.isEmpty()) {
            if (alertOnError) {
                Toast.makeText(this, title + " cannot be empty", Toast.LENGTH_SHORT).show();
                findViewById(id).requestFocus();
            }

            return null;
        }

        return val;
    }
}