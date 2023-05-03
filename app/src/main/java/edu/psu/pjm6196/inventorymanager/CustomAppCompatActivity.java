package edu.psu.pjm6196.inventorymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public abstract class CustomAppCompatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getToolbarTitleResId());
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(this::onBackButtonClicked);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.default_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected int getToolbarTitleResId() {
        return R.string.app_name;
    }

    protected void onBackButtonClicked(View view) {
        Intent intent = new Intent(this, getBackButtonClass());
        setReturnToActivityArgs(intent);
        startActivity(intent);
    }

    protected Class<?> getBackButtonClass() {
        return getCallingActivity().getClass();
    }

    protected void setReturnToActivityArgs(Intent intent) {
//         for passing data back to calling intent
        intent.putExtra("back_button_clicked", true);
    }
}
