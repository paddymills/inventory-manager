package edu.psu.pjm6196.inventorymanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public abstract class CustomAppCompatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(this.getLocalClassName(), "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle instanceState) {
        Log.d(this.getLocalClassName(), "Saving instance state...");
        super.onSaveInstanceState(instanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle instanceState) {
        Log.d(this.getLocalClassName(), "Restoring instance state...");
        super.onRestoreInstanceState(instanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getToolbarTitleResId());
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });
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

    protected boolean returnToCallingActivity(SetIntentExtrasListener listener) {
//        Intent intent = new Intent(this, ActivityDirector.getActivity(returnToActivity));
        Intent intent = new Intent(this, getCallingActivity().getClass());

        if (listener != null)
            listener.setExtras(intent);

        setResult(RESULT_OK, intent);
        finish();

        return true;
    }

    public interface SetIntentExtrasListener {
        void setExtras(Intent intent);
    }
}
