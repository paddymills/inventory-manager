package edu.psu.pjm6196.inventorymanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar
        Toolbar headerToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(headerToolbar);

        findViewById(R.id.btn_query)
            .setOnClickListener(
                v -> startActivity(new Intent(this, BarcodesListActivity.class))
            );
    }
}