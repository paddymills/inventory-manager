package edu.psu.pjm6196.inventorymanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends CustomAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_query)
            .setOnClickListener(
                v -> startActivity(new Intent(this, BarcodesListActivity.class))
            );

        findViewById(R.id.btn_move)
            .setOnClickListener(v -> startActivity(new Intent(this, ScanActivity.class)));
    }
}