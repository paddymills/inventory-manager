package edu.psu.pjm6196.inventorymanager.db;

import android.os.Parcel;
import android.os.Parcelable;

public class Material implements Parcelable {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.material_master);
        dest.writeString(this.location);
        dest.writeString(this.grade);
        dest.writeString(this.heat_number);
        dest.writeString(this.po_number);
    }

    public void readFromParcel(Parcel source) {
        this.material_master = source.readString();
        this.location = source.readString();
        this.grade = source.readString();
        this.heat_number = source.readString();
        this.po_number = source.readString();
    }

    protected Material(Parcel in) {
        this.material_master = in.readString();
        this.location = in.readString();
        this.grade = in.readString();
        this.heat_number = in.readString();
        this.po_number = in.readString();
    }

    public static final Parcelable.Creator<Material> CREATOR = new Parcelable.Creator<Material>() {
        @Override
        public Material createFromParcel(Parcel source) {
            return new Material(source);
        }

        @Override
        public Material[] newArray(int size) {
            return new Material[size];
        }
    };
}
