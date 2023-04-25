package edu.psu.pjm6196.inventorymanager.db;

public class Material {
    public Material(String material_master, String location, String grade, String heat_number, String po_number) {
        this.material_master = material_master;
        this.location = location;
        this.grade = grade;
        this.heat_number = heat_number;
        this.po_number = po_number;
    }

    public String material_master;
    public String location;
    public String grade;
    public String heat_number;
    public String po_number;
}
