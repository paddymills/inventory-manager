package edu.psu.pjm6196.inventorymanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import edu.psu.pjm6196.inventorymanager.db.BarcodeDatabase;

public class MainActivity extends ActivityBase {

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Move material intent and result
        ActivityResultLauncher<Intent> getId = registerForActivityResult(
            new ActivityResultContract<Intent, String>() {
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, Intent intent) {
                    intent.putExtra("calling_activity_intent", ScanActivity.CallingActivityIntent.MOVE_MATERIAL);

                    return intent;
                }

                @Override
                public String parseResult(int i, @Nullable Intent intent) {
                    assert intent != null;
                    return intent.getStringExtra("barcode_id");
                }
            },
            result -> {
                Log.d(TAG, "got result: " + result);

                handleMoveMaterial(result);
            });

        // Find(query) material intent and result
        ActivityResultLauncher<Intent> findMaterials = registerForActivityResult(
            new ActivityResultContract<Intent, ArrayList<String>>() {
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, Intent intent) {
                    intent.putExtra("calling_activity_intent", ScanActivity.CallingActivityIntent.FIND_MATERIAL);

                    return intent;
                }

                @Override
                public ArrayList<String> parseResult(int i, @Nullable Intent intent) {
                    assert intent != null;
                    return intent.getStringArrayListExtra("barcode_ids");
                }
            },
            result -> {
                Log.d(TAG, "got result: " + result);
                // TODO: launch BarcodeListActivity and filter it based on results
            });

        // Take inventory intent and result
        ActivityResultLauncher<Intent> takeInventory = registerForActivityResult(
            new ActivityResultContract<Intent, ArrayList<String>>() {
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, Intent intent) {
                    intent.putExtra("calling_activity_intent", ScanActivity.CallingActivityIntent.TAKE_INVENTORY);

                    return intent;
                }

                @Override
                public ArrayList<String> parseResult(int i, @Nullable Intent intent) {
                    if ( intent == null )
                        return new ArrayList<>();

//                    assert intent != null;
                    return intent.getStringArrayListExtra("barcode_ids");
                }
            },
            result -> {
                Log.d(TAG, "got result: " + result);

                new AlertDialog.Builder(this)
                    .setTitle("Barcodes Scanned")
                    .setMessage(String.join("\n", result))
                    .setPositiveButton("Ok", (dialog, i) -> {
                    })
                    .create()
                    .show();
                // TODO: display list of IDs (maybe re-use BarcodeListActivity with checkboxes?)
            });

        // button activity launchers
        findViewById(R.id.btn_list)
            .setOnClickListener(
                v -> startActivity(new Intent(this, BarcodesListActivity.class)));
        findViewById(R.id.btn_move)
            .setOnClickListener(
                v -> getId.launch(new Intent(this, ScanActivity.class)));
        findViewById(R.id.btn_launch_scanner)
            .setOnClickListener(
                v -> findMaterials.launch(new Intent(this, ScanActivity.class)));
        findViewById(R.id.btn_inventory)
            .setOnClickListener(
                v -> takeInventory.launch(new Intent(this, TakeInventoryActivity.class)));
    }

    private void handleMoveMaterial(String id_hash) {
        BarcodeDatabase.ensureInstanceIsSet(this);
        BarcodeDatabase.getBarcodeByIdHash(
            id_hash,
            barcode -> {
                EditText locInput = new EditText(this);
                locInput.setInputType(InputType.TYPE_CLASS_TEXT);
                locInput.setText(barcode.material.location);

                new AlertDialog.Builder(this)
                    .setTitle("Change Location")
                    .setView(locInput)
                    .setPositiveButton("Submit", (dialog, id) -> {
                        barcode.material.location = locInput.getText().toString();

                        BarcodeDatabase.update(barcode);
                        Toast.makeText(
                            this,
                            String.format("%s moved to %s", barcode.id_hash, barcode.material.location),
                            Toast.LENGTH_SHORT
                        ).show();
                    })
                    .setNegativeButton("Cancel", (dialog, id) -> {
                    })
                    .show();
            }
        );


    }
}