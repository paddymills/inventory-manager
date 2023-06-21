package edu.psu.pjm6196.inventorymanager.scanners;

import android.os.Bundle;

public class AddBarcodeScanActivity extends BaseScanActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected boolean isSingleBarcodeScanUseCase() {
        return true;
    }

    @Override
    protected boolean returnResult() {
        return returnToCallingActivity(intent -> {
            intent.putExtra("barcode_id", barcodes.getSelectedBarcodeIds().get(0));
        });
    }
}