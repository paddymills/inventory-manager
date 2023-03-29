package edu.psu.pjm6196.inventorymanager.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "barcodes")
public class Barcode {

    public Barcode(int id, String material, String heat_number, String po_number) {
        this.id = id;
        this.material = material;
        this.heat_number = heat_number;
        this.po_number = po_number;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "material")
    public String material;

    @ColumnInfo(name = "heat_number")
    public String heat_number;

    @ColumnInfo(name = "po_number")
    public String po_number;

}
