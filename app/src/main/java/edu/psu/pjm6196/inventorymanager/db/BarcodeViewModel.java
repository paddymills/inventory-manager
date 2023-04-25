package edu.psu.pjm6196.inventorymanager.db;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class BarcodeViewModel extends AndroidViewModel {
    private LiveData<List<RawBarcode>> barcodes;

    public BarcodeViewModel(Application app) {
        super(app);
        barcodes = BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getAll();
    }

    public void filterBarcodes(boolean filtered) {
        if ( filtered )
            barcodes = BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getByMaterial("50/50W-0100");
        else
            barcodes = BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getAll();
    }

    public LiveData<List<RawBarcode>> getByMaterialSearch(String search) {
        return BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getByMaterial(search);
    }

    public LiveData<List<RawBarcode>> getAllBarcodes() {
        return barcodes;
    }
}
