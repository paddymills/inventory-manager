package edu.psu.pjm6196.inventorymanager.utils;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import edu.psu.pjm6196.inventorymanager.db.Barcode;
import edu.psu.pjm6196.inventorymanager.db.BarcodeDAO;

public class Filters {

    private interface FilterExecutorListener {
        LiveData<List<Barcode>> execFilter(BarcodeDAO dao);
    }

    public abstract static class FilterBase {
        protected FilterExecutorListener executor;

        public LiveData<List<Barcode>> execFilter(BarcodeDAO dao) {
            return executor.execFilter(dao);
        }
    }

    public static class ByIdHashes extends FilterBase {
        public ByIdHashes(ArrayList<String> ids) {
            executor = dao -> dao.getByIdHashes(ids);
        }
    }

    public static class ByMaterial extends FilterBase {
        public ByMaterial(String material) {
            executor = dao -> dao.getByMaterial(material);
        }
    }
}
