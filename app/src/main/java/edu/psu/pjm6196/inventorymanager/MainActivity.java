package edu.psu.pjm6196.inventorymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
            .setOnClickListener(v -> startActivity(new Intent(this, ScanActivity.class)));

        findViewById(R.id.btn_launch_scanner)
            .setOnClickListener(v -> {
                Intent intent = new Intent(this, ScanActivity.class);
                intent.putExtra("calling_activity_intent", ScanActivity.CallingActivityIntent.FindMaterial);

                startActivity(intent);
            });
    }
}