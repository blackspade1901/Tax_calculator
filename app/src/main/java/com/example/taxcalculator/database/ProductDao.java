package com.example.taxcalculator.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.taxcalculator.models.ProductItem;
import java.util.List;

@Dao
public interface ProductDao {

    @Insert
    void insert(ProductItem product);

    // FIX: Changed 'products' to 'product_table'
    @Query("SELECT * FROM product_table ORDER BY id DESC")
    List<ProductItem> getAll();

    // FIX: Changed 'products' to 'product_table'
    @Query("DELETE FROM product_table")
    void deleteAll();

    // This is the new method for the Offline Scanner
    @Query("SELECT * FROM product_table WHERE barcode = :code LIMIT 1")
    ProductItem findByBarcode(String code);
}