package edu.psu.pjm6196.inventorymanager.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BarcodeDAO {

    @Query("SELECT * FROM barcodes ORDER BY hash COLLATE NOCASE, id")
    LiveData<List<RawBarcode>> getAll();

    @Insert
    void insert(RawBarcode... barcodes);

    @Update
    void update(RawBarcode... barcodes);

    @Delete
    void delete(RawBarcode... barcodes);

    @Query("SELECT * FROM barcodes WHERE id = :barcode_id")
    RawBarcode getById(int barcode_id);

    @Query("SELECT * FROM barcodes WHERE material_master LIKE :search_term || '%'")
    LiveData<List<RawBarcode>> getByMaterial(String search_term);
}
