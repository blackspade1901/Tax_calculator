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

    @Query("SELECT * FROM products ORDER BY id DESC")
    List<ProductItem> getAll();

    @Query("DELETE FROM products")
    void deleteAll();
}
