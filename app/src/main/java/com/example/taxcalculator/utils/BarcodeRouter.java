package com.example.taxcalculator.utils;

/**
 * Utility class for routing barcode scanning requests.
 * Determines the type of product based on barcode prefixes (e.g., books, Indian retail).
 * Helps in optimizing which API or logic to use for lookup.
 */
public class BarcodeRouter {

    /**
     * Enumeration of possible product types based on barcode analysis.
     */
    public enum ProductType {
        /**
         * Represents Indian retail products (typically starting with 890).
         */
        INDIAN_RETAIL,

        /**
         * Represents books (typically ISBNs starting with 978).
         */
        BOOK,

        /**
         * Represents general global products that don't match specific prefixes.
         */
        GLOBAL_GENERAL
    }

    /**
     * Determines the product type based on the barcode string.
     *
     * @param barcode The scanned barcode string.
     * @return The determined ProductType (BOOK, INDIAN_RETAIL, or GLOBAL_GENERAL).
     */
    public static ProductType getRoute(String barcode) {
        if (barcode == null) return ProductType.GLOBAL_GENERAL;
        if (barcode.startsWith("978")) return ProductType.BOOK;
        if (barcode.startsWith("890")) return ProductType.INDIAN_RETAIL;
        return ProductType.GLOBAL_GENERAL;
    }
}