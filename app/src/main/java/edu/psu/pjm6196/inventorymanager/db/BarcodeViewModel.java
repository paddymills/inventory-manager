package edu.psu.pjm6196.inventorymanager.db;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class BarcodeViewModel extends AndroidViewModel {
    private static final String TAG = "BarcodeViewModel";
    public boolean isFiltered;
    private LiveData<List<Barcode>> barcodes;

    public BarcodeViewModel(Application app) {
        super(app);
        displayAll();
    }

    public void displayAll() {
        barcodes = BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getAll();
    }

    public void setFilter(SetFilterListener listener) {
        Log.d(TAG, "Using filter 2.0");

        if (listener == null) {
            isFiltered = false;
            displayAll();

            return;
        }

        barcodes = listener.setFilter(
            BarcodeDatabase.getDatabase(getApplication()).barcodeDAO()
        );
    }

    public void filterBarcodes(boolean filtered) {
        Log.d(TAG, "Using old filters");
        isFiltered = filtered;

        if (isFiltered)
            barcodes = BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getByMaterial("50/50W-0010");
        else
            displayAll();
    }

    public void filterByIdHashBarcodes(boolean filtered, ArrayList<String> id_hashes) {
        Log.d(TAG, "Using old filter");
        isFiltered = filtered;

        if (filtered)
            barcodes = BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getByIdHashes(id_hashes);
        else
            displayAll();
    }

    public LiveData<List<Barcode>> getByMaterialSearch(String search) {
        return BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getByMaterial(search);
    }

    public LiveData<List<Barcode>> getBarcodes() {
        return barcodes;
    }

    public interface SetFilterListener {
        LiveData<List<Barcode>> setFilter(BarcodeDAO dao);
    }
}
