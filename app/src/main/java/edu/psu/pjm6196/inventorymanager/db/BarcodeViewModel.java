package edu.psu.pjm6196.inventorymanager.db;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class BarcodeViewModel extends AndroidViewModel {
    private LiveData<List<Barcode>> barcodes;

    public BarcodeViewModel(Application app) {
        super(app);
        barcodes = BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getAll();
    }

    public void filterBarcodes(boolean filtered) {
        if ( filtered )
            barcodes = BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getByMaterial("50/50W-0010");
        else
            barcodes = BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getAll();
    }

    public void filterByIdHashBarcode(boolean filtered, String id_hash) {
        if ( filtered )
            barcodes = BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getByIdHash(id_hash);
        else
            barcodes = BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getAll();
    }
    public void filterByIdHashBarcodes(boolean filtered, ArrayList<String> id_hashes) {
        if ( filtered )
            barcodes = BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getByIdHashes(id_hashes);
        else
            barcodes = BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getAll();
    }

    public LiveData<List<Barcode>> getByMaterialSearch(String search) {
        return BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getByMaterial(search);
    }

    public LiveData<List<Barcode>> getAllBarcodes() {
        return barcodes;
    }
}
