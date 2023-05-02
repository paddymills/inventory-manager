package edu.psu.pjm6196.inventorymanager;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.util.Size;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends CustomAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            ListPreference pref = findPreference("camera_resolution");
            if ( pref != null ) {
                String[] entries = get_camera_resolution_entries();
                pref.setEntries(entries);
                pref.setEntryValues(entries);
            }
        }

        private String[] get_camera_resolution_entries() {
            CameraCharacteristics cameraChars = getCameraCharacteristics();

            String[] entries;
            if (cameraChars != null) {
                StreamConfigurationMap map = cameraChars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size[] outputSizes = map.getOutputSizes(SurfaceTexture.class);
                entries = new String[outputSizes.length];
                for (int i = 0; i < outputSizes.length; i++) {
                    entries[i] = outputSizes[i].toString();
                }
            } else {
                entries =
                    new String[] {
                        "2000x2000",
                        "1600x1600",
                        "1200x1200",
                        "1000x1000",
                        "800x800",
                        "600x600",
                        "400x400",
                        "200x200",
                        "100x100",
                    };
            }

            return entries;
        }

        public CameraCharacteristics getCameraCharacteristics() {
            try {
                FragmentActivity activity = getActivity();
                if ( activity == null )
                    return null;

                CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

                for (String id : cameraManager.getCameraIdList()) {
                    CameraCharacteristics cam_chars = cameraManager.getCameraCharacteristics(id);
                    Integer lensFacing = cam_chars.get(CameraCharacteristics.LENS_FACING);
                    if (lensFacing == null)
                        continue;

                    if ( lensFacing.equals(ScanActivity.cameraLens) )
                        return cam_chars;
                }
            } catch (CameraAccessException e) {
                // Accessing camera ID info got error
            }

            return null;
        }
    }
}