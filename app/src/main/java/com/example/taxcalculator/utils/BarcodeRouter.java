package com.example.taxcalculator.utils;

public class BarcodeRouter {
    public enum ProductType {
        INDIAN_RETAIL, BOOK, GLOBAL_GENERAL
    }

    public static ProductType getRoute(String barcode) {
        if (barcode.startsWith("978")) return ProductType.BOOK;
        if (barcode.startsWith("890")) return ProductType.INDIAN_RETAIL;
        return ProductType.GLOBAL_GENERAL;
    }
}