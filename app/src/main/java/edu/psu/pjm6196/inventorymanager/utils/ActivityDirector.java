package edu.psu.pjm6196.inventorymanager.utils;

import android.util.Log;

import edu.psu.pjm6196.inventorymanager.AddBarcodeActivity;
import edu.psu.pjm6196.inventorymanager.BarcodesListActivity;
import edu.psu.pjm6196.inventorymanager.CustomAppCompatActivity;
import edu.psu.pjm6196.inventorymanager.MainActivity;
import edu.psu.pjm6196.inventorymanager.ScanActivity;

public class ActivityDirector {
    private static final String TAG = "ActivityDirector";
    public static final String KEY = "return_to";

    // start numbering at 1 in case key is never set (defaults to 0)
    public static final int MAIN = 1;
    public static final int SCAN = 2;
    public static final int ADD  = 3;
    public static final int LIST = 4;
    public static final int DEFAULT = MAIN;
    private static final Class<?> DEFAULT_ACTIVITY = MainActivity.class;

    public static Class<?> getActivity(int key) {
        switch ( key ) {
            case MAIN:
                return MainActivity.class;
            case SCAN:
                return ScanActivity.class;
            case ADD:
                return AddBarcodeActivity.class;
            case LIST:
                return BarcodesListActivity.class;
            default:
                Log.e(TAG, "Unexpected return activity: " + key );
        }

        return DEFAULT_ACTIVITY;
    }

    public static int getActivityId(CustomAppCompatActivity instance) {
        switch (instance.getLocalClassName()) {
            case "MainActivity":
                return MAIN;
            case "ScanActivity":
                return SCAN;
            case "AddBarcodeActivity":
                return ADD;
            case "BarcodesListActivity":
                return LIST;
            default:
                Log.e(TAG, "Unexpected activity: " + instance.getLocalClassName() );
        }

        return DEFAULT;
    }
}
