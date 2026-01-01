package com.example.taxcalculator.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "product_table")
public class ProductItem implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    private String name;
    private String brand;
    private double price; // This is the MRP
    private double taxRate; // 0, 5, 12, 18, 40

    // NEW FIELD: Store the barcode!
    private String barcode;

    // Updated Constructor
    public ProductItem(String name, String brand, double price, double taxRate, String barcode) {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.taxRate = taxRate;
        this.barcode = barcode;
    }

    // Getters and Setters
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public double getPrice() { return price; }
    public double getTaxRate() { return taxRate; }
    public String getBarcode() { return barcode; }

    // Helpers for calculations
    public double getTaxAmount() { return (price * taxRate) / (100 + taxRate); }
    public double getNetPrice() { return price - getTaxAmount(); }
    public double getTotalPrice() { return price; }
}