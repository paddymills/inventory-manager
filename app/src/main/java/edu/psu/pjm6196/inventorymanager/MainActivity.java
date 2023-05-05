package edu.psu.pjm6196.inventorymanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import edu.psu.pjm6196.inventorymanager.db.BarcodeDatabase;
import edu.psu.pjm6196.inventorymanager.utils.ActivityDirector;

public class MainActivity extends CustomAppCompatActivity {

    public static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: implement taking inventory
        findViewById(R.id.btn_inventory).setVisibility(View.GONE);

        findViewById(R.id.btn_list)
            .setOnClickListener(
                v -> {
                    Intent intent = new Intent(this, BarcodesListActivity.class);
                    intent.putExtra(ActivityDirector.KEY, ActivityDirector.MAIN);

                    startActivity(intent);
                }
            );

        ActivityResultLauncher<Intent> getId = registerForActivityResult(
            new ActivityResultContract<Intent, String>() {
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, Intent intent) {
                    intent.putExtra(ActivityDirector.KEY, ActivityDirector.MAIN);
                    intent.putExtra("calling_activity_intent", ScanActivity.CallingActivityIntent.MOVE_MATERIAL.toString());

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

        findViewById(R.id.btn_move)
            .setOnClickListener(v -> {
                Intent intent = new Intent(this, ScanActivity.class);
                getId.launch(intent);
            });

        findViewById(R.id.btn_launch_scanner)
            .setOnClickListener(v -> {
                Intent intent = new Intent(this, ScanActivity.class);
                intent.putExtra(ActivityDirector.KEY, ActivityDirector.MAIN);
                intent.putExtra("calling_activity_intent", ScanActivity.CallingActivityIntent.FIND_MATERIAL.toString());

                startActivity(intent);
            });
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
                    .setNegativeButton("Cancel", (dialog, id) -> {})
                    .show();
            }
        );


    }
}