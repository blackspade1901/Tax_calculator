package com.example.taxcalculator.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.taxcalculator.models.ProductItem;
import java.util.List;

/**
 * Data Access Object (DAO) for accessing the product database.
 * Defines methods for inserting, querying, and deleting product records.
 * Used by the Room persistence library.
 */
@Dao
public interface ProductDao {

    /**
     * Inserts a new product into the database.
     *
     * @param product The ProductItem object to be inserted.
     */
    @Insert
    void insert(ProductItem product);

    /**
     * Retrieves all products from the database.
     * The results are ordered by ID in descending order (newest first).
     *
     * @return A list of all ProductItem objects.
     */
    @Query("SELECT * FROM product_table ORDER BY id DESC")
    List<ProductItem> getAll();

    /**
     * Deletes all products from the database.
     * This operation clears the entire scan history.
     */
    @Query("DELETE FROM product_table")
    void deleteAll();

    /**
     * Finds a specific product by its barcode.
     * Useful for checking if a scanned item already exists locally.
     *
     * @param code The barcode string to search for.
     * @return The matching ProductItem, or null if not found.
     */
    @Query("SELECT * FROM product_table WHERE barcode = :code LIMIT 1")
    ProductItem findByBarcode(String code);
}