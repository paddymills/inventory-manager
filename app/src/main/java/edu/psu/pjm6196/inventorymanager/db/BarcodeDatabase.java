package edu.psu.pjm6196.inventorymanager.db;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

// this code is mostly adapted from https://github.com/jeremyblum/cs_jokes_with_room/blob/master/app/src/main/java/edu/psu/jjb24/csjokes/db/JokeDatabase.java
// I made some changes:
//   - hard coded createBarcodeTable because I did not implement DefaultContent
//   - some android studio recommendations
//      - made INSTANCE volatile
//      - made createBarcodeDatabaseCallback final

@Database(entities = {Barcode.class}, version = 1, exportSchema = false)
public abstract class BarcodeDatabase extends RoomDatabase {
    public interface BarcodeListener {
        void onBarcodeReturned(Barcode barcode);
    }

    public abstract BarcodeDAO barcodeDAO();

    private static volatile BarcodeDatabase INSTANCE;

    public static BarcodeDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (BarcodeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        BarcodeDatabase.class,
                        "barcode_database"
                    )
                        .addCallback(createBarcodeDatabaseCallback)
                        .build();
                }
            }
        }

        return INSTANCE;
    }

    private static final RoomDatabase.Callback createBarcodeDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            createBarcodeTable();
        }
    };

    private static void createBarcodeTable() {
        insert(new Barcode(0, "0x000", new Material("50/50W-0008", "I2", "50/50W", "D4680", "4500239675")));
        insert(new Barcode(0, "0x001", new Material("50/50W-0008", "I2", "50/50W", "D4680", "4500239675")));
        insert(new Barcode(0, "0x002", new Material("50/50W-0008", "I2", "50/50W", "D4680", "4500239675")));
        insert(new Barcode(0, "0x003", new Material("50/50W-0008", "I2", "50/50W", "D4680", "4500239675")));
    }

    public static void getBarcode(int id, BarcodeListener listener) {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                super.handleMessage(message);
                listener.onBarcodeReturned((Barcode) message.obj);
            }
        };

        (new Thread(() -> {
            Message message = handler.obtainMessage();
            message.obj = INSTANCE.barcodeDAO().getById(id);

            handler.sendMessage(message);
        })).start();
    }

    public static void insert(Barcode barcode) {
        (new Thread(() -> INSTANCE.barcodeDAO().insert(barcode))).start();
    }

    public static void update(Barcode barcode) {
        (new Thread(() -> INSTANCE.barcodeDAO().update(barcode))).start();
    }

    public static void delete(Barcode barcode) {
        (new Thread(() -> INSTANCE.barcodeDAO().delete(barcode))).start();
    }
}
