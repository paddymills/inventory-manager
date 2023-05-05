package edu.psu.pjm6196.inventorymanager.db;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "barcodes")
public class Barcode implements Parcelable {

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

    public String title() {
        return this.id + ": " + this.id_hash + " (" + this.material.material_master + ")";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.id_hash);
        dest.writeParcelable(this.material, flags);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readInt();
        this.id_hash = source.readString();
        this.material = source.readParcelable(Material.class.getClassLoader());
    }

    protected Barcode(Parcel in) {
        this.id = in.readInt();
        this.id_hash = in.readString();
        this.material = in.readParcelable(Material.class.getClassLoader());
    }

    public static final Parcelable.Creator<Barcode> CREATOR = new Parcelable.Creator<Barcode>() {
        @Override
        public Barcode createFromParcel(Parcel source) {
            return new Barcode(source);
        }

        @Override
        public Barcode[] newArray(int size) {
            return new Barcode[size];
        }
    };
}
