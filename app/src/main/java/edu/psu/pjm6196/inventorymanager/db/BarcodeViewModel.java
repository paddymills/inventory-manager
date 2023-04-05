package edu.psu.pjm6196.inventorymanager.db;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class BarcodeViewModel extends AndroidViewModel {
    private LiveData<List<Barcode>> barcodes;

    public BarcodeViewModel(Application app) {
        super(app);
        barcodes = BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getAll();
    }

    public LiveData<List<Barcode>> getAllBarcodes() {
        return barcodes;
    }
}
