package edu.psu.pjm6196.inventorymanager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class AddBarcodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_barcode);

        // setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
    }
}