package edu.psu.pjm6196.inventorymanager.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

public class Material {
    public String material_master;
    public String location;
    public String grade;
    public String heat_number;
    public String po_number;
}
