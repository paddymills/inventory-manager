package edu.psu.pjm6196.inventorymanager.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Size;

import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.preference.PreferenceManager;

import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.Set;

public class PreferenceUtils {
    private static final int DEFAULT_LIFETIME = 1;
    private static final int DEFAULT_FORMAT = Barcode.FORMAT_PDF417;

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

    public static int[] getAcceptedBarcodeFormats(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Set<String> accepted = prefs.getStringSet("accepted_barcode_formats", null);

        if (accepted == null)
            return new int[]{DEFAULT_FORMAT};

        return accepted
            .stream()
            .mapToInt(PreferenceUtils::getBarcodeFormat)
            .toArray();
    }

    private static int getBarcodeFormat(String format) {
        return switch (format) {
            case "AZTEC" -> Barcode.FORMAT_AZTEC;
            case "CODABAR" -> Barcode.FORMAT_CODABAR;
            case "CODE_39" -> Barcode.FORMAT_CODE_39;
            case "CODE_93" -> Barcode.FORMAT_CODE_93;
            case "CODE_128" -> Barcode.FORMAT_CODE_128;
            case "DATA_MATRIX" -> Barcode.FORMAT_DATA_MATRIX;
            case "EAN_8" -> Barcode.FORMAT_EAN_8;
            case "EAN_13" -> Barcode.FORMAT_EAN_13;
            case "ITF" -> Barcode.FORMAT_ITF;
            case "PDF_417" -> Barcode.FORMAT_PDF417;
            case "QR_CODE" -> Barcode.FORMAT_QR_CODE;
            case "UPC_A" -> Barcode.FORMAT_UPC_A;
            case "UPC_E" -> Barcode.FORMAT_UPC_E;
            default -> DEFAULT_FORMAT;
        };
    }
}
