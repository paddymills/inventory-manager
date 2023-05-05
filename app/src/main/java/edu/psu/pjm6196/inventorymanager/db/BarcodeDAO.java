package edu.psu.pjm6196.inventorymanager.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface BarcodeDAO {

    @Query("SELECT * FROM barcodes ORDER BY id COLLATE NOCASE, id")
    LiveData<List<Barcode>> getAll();

    @Insert
    void insert(Barcode... barcodes);

    @Update
    void update(Barcode... barcodes);

    @Delete
    void delete(Barcode... barcodes);

    @Query("SELECT * FROM barcodes WHERE id = :barcode_id")
    LiveData<List<Barcode>> getById(int barcode_id);

    @Query("SELECT * FROM barcodes WHERE hash = :id_hash")
    Barcode getByIdHash(String id_hash);

    @Query("SELECT * FROM barcodes WHERE hash IN (:id_hash)")
    LiveData<List<Barcode>> getByIdHashes(ArrayList<String> id_hash);

    @Query("SELECT * FROM barcodes WHERE material_master LIKE :material || '%'")
    LiveData<List<Barcode>> getByMaterial(String material);

    @Query("SELECT * FROM barcodes WHERE location = :location")
    LiveData<List<Barcode>> getByLocation(String location);

    @Query("SELECT * FROM barcodes WHERE heat_number LIKE :heat || '%'")
    LiveData<List<Barcode>> getByHeatNumber(String heat);

    @Query("SELECT * FROM barcodes WHERE material_master LIKE :material || '%' AND location = :location")
    LiveData<List<Barcode>> getByMaterialAndLocation(String material, String location);

    @Query("SELECT * FROM barcodes WHERE material_master LIKE :material || '%' AND heat_number LIKE :heat || '%'")
    LiveData<List<Barcode>> getByMaterialAndHeatNumber(String material, String heat);

    @Query("SELECT * FROM barcodes WHERE location = :location AND heat_number LIKE :heat || '%'")
    LiveData<List<Barcode>> getByLocationAndHeatNumber(String location, String heat);

    @Query("SELECT * FROM barcodes WHERE material_master LIKE :material || '%' AND location = :location AND heat_number LIKE :heat || '%'")
    LiveData<List<Barcode>> getByMaterialAndLocationAndHeatNUmber(String material, String location, String heat);

}
