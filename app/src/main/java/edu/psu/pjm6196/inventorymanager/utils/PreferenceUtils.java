package edu.psu.pjm6196.inventorymanager.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Size;

import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.preference.PreferenceManager;

public class PreferenceUtils {
    private static final int DEFAULT_LIFETIME = 1;

    public static ResolutionSelector getTargetCameraResolution(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String targetResolution = prefs.getString("camera_resolution", null);

        if (targetResolution != null) {
            return new ResolutionSelector.Builder()
                .setResolutionStrategy(new ResolutionStrategy(
                    Size.parseSize(targetResolution),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER
                ))
                .build();
        }

        return null;
    }

    public static boolean isLivePreviewEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getBoolean("live_preview", true);
    }

    public static boolean showDetectionInfo(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getBoolean("debug_mode", false);
    }

    public static int getBarcodeLifetime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return Integer.parseInt(
            prefs.getString("lifetime_duration", String.valueOf(DEFAULT_LIFETIME))
        ) * 1000;
    }
}
