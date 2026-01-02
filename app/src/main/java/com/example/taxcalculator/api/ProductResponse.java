package com.example.taxcalculator.api;

import com.google.gson.annotations.SerializedName;

/**
 * Data model for the product response from Open Food/Beauty/Product Facts API.
 * Maps the JSON response from the API to Java objects.
 */
public class ProductResponse {

    /**
     * Status code of the response.
     * Typically, 1 means found, 0 means not found.
     */
    @SerializedName("status")
    public int status;

    /**
     * The inner data object containing detailed product information.
     */
    @SerializedName("product")
    public ProductData product;

    /**
     * Inner class representing the specific product details within the response.
     */
    public class ProductData {
        /**
         * The primary product name in the local language.
         */
        @SerializedName("product_name")
        public String productName;

        /**
         * Fallback product name in English.
         */
        @SerializedName("product_name_en")
        public String productNameEn;

        /**
         * The brand(s) of the product.
         */
        @SerializedName("brands")
        public String brands;

        /**
         * Helper method to determine the best available name for the product.
         * Prioritizes the local product name, falls back to the English name,
         * or returns "Unknown Product" if neither is available.
         *
         * @return The best available name string.
         */
        public String getBestName() {
            if (productName != null && !productName.isEmpty()) return productName;
            if (productNameEn != null && !productNameEn.isEmpty()) return productNameEn;
            return "Unknown Product";
        }
    }
}