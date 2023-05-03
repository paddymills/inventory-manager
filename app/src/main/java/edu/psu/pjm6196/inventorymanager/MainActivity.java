package edu.psu.pjm6196.inventorymanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import edu.psu.pjm6196.inventorymanager.db.Barcode;
import edu.psu.pjm6196.inventorymanager.db.BarcodeDatabase;

public class MainActivity extends CustomAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: implement taking inventory
        findViewById(R.id.btn_inventory).setVisibility(View.GONE);

        findViewById(R.id.btn_list)
            .setOnClickListener(
                v -> startActivity(new Intent(this, BarcodesListActivity.class))
            );

        findViewById(R.id.btn_move)
            .setOnClickListener(v -> {
                Intent intent = new Intent(this, ScanActivity.class);
                intent.putExtra("calling_activity", "Main");
                intent.putExtra("calling_activity_intent", ScanActivity.CallingActivityIntent.MOVE_MATERIAL.toString());

                startActivity(intent);
            });

        findViewById(R.id.btn_launch_scanner)
            .setOnClickListener(v -> {
                Intent intent = new Intent(this, ScanActivity.class);
                intent.putExtra("calling_activity", "Main");
                intent.putExtra("calling_activity_intent", ScanActivity.CallingActivityIntent.FIND_MATERIAL.toString());

                startActivity(intent);
            });

        Intent callingIntent = getIntent();
        if ( callingIntent != null && callingIntent.hasExtra("barcode_id") ) {
            // make sure database is loaded
            BarcodeDatabase.ensureInstanceIsSet(this);

            // TODO: handle barcode not in database
            BarcodeDatabase.getBarcodeByIdHash(
                callingIntent.getStringExtra("barcode_id"),
                this::handleMoveMaterial
            );
        }

    }

    private void handleMoveMaterial(Barcode barcode) {
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
}