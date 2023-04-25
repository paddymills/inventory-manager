package edu.psu.pjm6196.inventorymanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

public class AddBarcodeActivity extends CustomAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_barcode);
    }

    @Override

    protected void onBackButtonClicked() {
        startActivity(new Intent(this, BarcodesListActivity.class));
    }
}