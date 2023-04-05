package edu.psu.pjm6196.inventorymanager.db;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "barcodes")
public class Barcode {

    public Barcode(int id, String id_hash, Material material) {
        this.id = id;
        this.id_hash = id_hash;
        this.material = material;
    }

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "hash")
    public String id_hash;

    @Embedded
    public Material material;
}
