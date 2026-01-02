package com.example.taxcalculator.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.taxcalculator.models.ProductItem;

/**
 * The Room Database class for the application.
 * Manages the SQLite database instance and provides access to the Data Access Objects (DAOs).
 * Uses the Singleton pattern to ensure only one instance of the database exists.
 */
@Database(entities = {ProductItem.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Provides the Data Access Object (DAO) for the Product table.
     * @return The ProductDao instance.
     */
    public abstract ProductDao productDao();

    /**
     * Volatile instance variable to ensure atomic access to the singleton.
     */
    private static volatile AppDatabase INSTANCE;

    /**
     * Retrieves the singleton instance of the AppDatabase.
     * Creates the database if it doesn't exist, using a destructive migration strategy.
     *
     * @param context The application context.
     * @return The singleton AppDatabase instance.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "tax_history_db")
                            // Allows database queries on the main thread (Note: Not recommended for large operations in production)
                            .allowMainThreadQueries()
                            // Wipes and rebuilds the database if the schema version changes, preventing crashes during development
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}