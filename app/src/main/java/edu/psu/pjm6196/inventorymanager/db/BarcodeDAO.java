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
    LiveData<List<Barcode>> getAll();

    @Insert
    void insert(Barcode... barcodes);

    @Update
    void update(Barcode... barcodes);

    @Delete
    void delete(Barcode... barcodes);

    @Query("SELECT * FROM barcodes WHERE id = :barcode_id")
    Barcode getById(int barcode_id);
}
