package com.example.taxcalculator.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.taxcalculator.models.ProductItem;

// 1. UPDATE VERSION TO 2 (Because we added 'barcode' column)
@Database(entities = {ProductItem.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ProductDao productDao();
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "tax_history_db")
                            .allowMainThreadQueries()
                            // 2. ADD THIS LINE: Deletes old DB if schema changes (Fixes the crash)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}