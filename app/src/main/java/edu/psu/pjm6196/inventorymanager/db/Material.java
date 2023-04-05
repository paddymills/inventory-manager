package edu.psu.pjm6196.inventorymanager.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

public class Material {
    public Material(String matl, String loc, String grade, String heat, String po) {
        this.material_master = matl;
        this.location = loc;
        this.grade = grade;
        this.heat_number = heat;
        this.po_number = po;
    }

    public String material_master;
    public String location;
    public String grade;
    public String heat_number;
    public String po_number;
}
