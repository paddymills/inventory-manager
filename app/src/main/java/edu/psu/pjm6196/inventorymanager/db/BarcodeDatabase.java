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
// I made some changes because of android studio recommendations
//     - made INSTANCE volatile
//     - made createBarcodeDatabaseCallback final

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

    public static void ensureInstanceIsSet(Context context) {
        getDatabase(context);
    }

    private static void createBarcodeTable() {
        for (int i=0; i<TestData.ids.length; i++)
            insert( new Barcode(0, TestData.ids[i], TestData.materials[i]) );
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
            Message msg = handler.obtainMessage();
            msg.obj = INSTANCE.barcodeDAO().getById(id);

            handler.sendMessage(msg);
        })).start();
    }

    public static void getBarcodeByIdHash(String id_hash, BarcodeListener listener) {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                listener.onBarcodeReturned((Barcode) msg.obj);
            }
        };

        (new Thread(() -> {
            Message msg = handler.obtainMessage();
            msg.obj = INSTANCE.barcodeDAO().getByIdHash(id_hash);
            handler.sendMessage(msg);
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
