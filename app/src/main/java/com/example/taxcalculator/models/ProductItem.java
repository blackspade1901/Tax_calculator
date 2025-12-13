package com.example.taxcalculator.models;

public class ProductItem {
    private String name;
    private String brand;
    private double totalPrice;
    private double taxRate;

    public ProductItem(String name, String brand, double totalPrice, double taxRate) {
        this.name = name;
        this.brand = brand;
        this.totalPrice = totalPrice;
        this.taxRate = taxRate;
    }

    public double getTaxAmount() {
        return (totalPrice * taxRate) / 100.0;
    }

    public double getNetPrice() {
        return totalPrice - getTaxAmount();
    }

    public String getName() { return name; }
    public String getBrand() { return brand; }
    public double getTotalPrice() { return totalPrice; }
    public double getTaxRate() { return taxRate; }
}
