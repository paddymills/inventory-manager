package edu.psu.pjm6196.inventorymanager.db;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import edu.psu.pjm6196.inventorymanager.utils.Filters.FilterBase;

public class BarcodeViewModel extends AndroidViewModel {

    private static final String TAG = "BarcodeViewModel";
    public boolean isFiltered;
    public FilterBase filter;
    private LiveData<List<Barcode>> barcodes;
    private SetBarcodeModelObserverListener setObserverListener;

    public BarcodeViewModel(Application app) {
        super(app);
        displayAll();
    }

    public void setLiveDataObserver(SetBarcodeModelObserverListener listener) {
        setObserverListener = listener;

        // since the listener is not set when the constructor is called,
        //      displayAll will not bind the observer
        // so we will call the observer now
        setObserverListener.setObserver(barcodes);
    }

    public void displayAll() {
        setBarcodes(
            BarcodeDatabase.getDatabase(getApplication()).barcodeDAO().getAll());

        isFiltered = false;
    }

    /**
     * @param filter barcode database filter
     *               loads a filter and executes it
     */
    public void setFilter(FilterBase filter) {
        loadFilter(filter);

        isFiltered = false;
        try {
            toggleFilter();
        }

        // filter will never be null because it was just set
        catch (NoFilterSetException ignored) {
        }
    }

    /**
     * @param filter barcode database filter
     *               loads the filter, but does not execute it.
     *               this is useful for restoring state where a filter was created, but disabled
     */
    public void loadFilter(FilterBase filter) {
        Log.d(TAG, "Using filter 2.0");
        this.filter = filter;
    }

    public void toggleFilter() throws NoFilterSetException {
        if (isFiltered)
            displayAll();

        else if (filter == null)
            // cannot add a null filter
            throw new NoFilterSetException();

        else {
            setBarcodes(
                filter.execFilter(
                    BarcodeDatabase.getDatabase(getApplication()).barcodeDAO()));

            isFiltered = true;
        }
    }

    public LiveData<List<Barcode>> getBarcodes() {
        return barcodes;
    }

    private void setBarcodes(LiveData<List<Barcode>> barcodesList) {
        barcodes = barcodesList;

        if (setObserverListener != null)
            setObserverListener.setObserver(barcodes);
    }

    public interface SetBarcodeModelObserverListener {
        void setObserver(LiveData<List<Barcode>> barcodesList);
    }

    public static class NoFilterSetException extends Exception {
    }
}
