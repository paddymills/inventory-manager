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

import edu.psu.pjm6196.inventorymanager.utils.ActivityDirector;

public abstract class CustomAppCompatActivity extends AppCompatActivity {

    public interface SetIntentExtrasListener {
        void setExtras(Intent intent);
    }
    protected int returnToActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ( savedInstanceState != null ) {
            Log.d(this.getLocalClassName(), "Restoring returnToActivity");
            returnToActivity = savedInstanceState.getInt(ActivityDirector.KEY);
        }

        Intent callingIntent = getIntent();
        if ( callingIntent == null )
            Log.d("CustomAppCompat", "callingIntent is null");
        else if ( callingIntent.hasExtra(ActivityDirector.KEY) )
            // already checked that key exists, so DEFAULT will probably not be set
            returnToActivity = callingIntent.getIntExtra(ActivityDirector.KEY, ActivityDirector.DEFAULT);

//        Log.d(this.getLocalClassName(), "returnToActivity is " + ActivityDirector.getActivity(returnToActivity));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle instanceState) {
        Log.d(this.getLocalClassName(), "Saving instance state...");
        super.onSaveInstanceState(instanceState);

        instanceState.putInt(ActivityDirector.KEY, returnToActivity);
    }

    @Override
    protected void onRestoreInstanceState(Bundle instanceState) {
        Log.d(this.getLocalClassName(), "Restoring instance state...");
        super.onRestoreInstanceState(instanceState);

        if ( instanceState != null && instanceState.containsKey(ActivityDirector.KEY) )
            returnToActivity = instanceState.getInt(ActivityDirector.KEY);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getToolbarTitleResId());
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(view -> finish());
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
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(ActivityDirector.KEY, ActivityDirector.getActivityId(this));

            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected int getToolbarTitleResId() {
        return R.string.app_name;
    }

    protected boolean returnToCallingActivity(SetIntentExtrasListener listener) {
        Intent intent = new Intent(this, ActivityDirector.getActivity(returnToActivity));
        listener.setExtras(intent);
//        startActivity(intent);

        setResult(RESULT_OK, intent);
        finish();

        return true;
    }

    protected void setReturnToActivityArgs(Intent intent) {
//         for passing data back to calling intent
        intent.putExtra("back_button_clicked", true);
    }
}
