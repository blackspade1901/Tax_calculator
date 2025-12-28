package com.example.taxcalculator.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class ProductItem implements  java.io.Serializable{
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "brand")
    private String brand;

    @ColumnInfo(name = "total_price")
    private double totalPrice;

    @ColumnInfo(name = "tax_rate")
    private double taxRate;

    public ProductItem(String name, String brand, double totalPrice, double taxRate) {
        this.name = name;
        this.brand = brand;
        this.totalPrice = totalPrice;
        this.taxRate = taxRate;
    }

    public int getId(){ return id; }
    public void setId(int id){ this.id = id; }

    public String getName() { return name; }
    public String getBrand() { return brand; }
    public double getTotalPrice() { return totalPrice; }
    public double getTaxRate() { return taxRate; }

    public double getTaxAmount() {
        return (totalPrice * taxRate) / 100.0;
    }

    public double getNetPrice() {
        return totalPrice - getTaxAmount();
    }
}
