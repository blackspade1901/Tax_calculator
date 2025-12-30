package com.example.taxcalculator.api;

import com.google.gson.annotations.SerializedName;

public class ProductResponse {
    @SerializedName("status")
    public int status;

    @SerializedName("product")
    public ProductData product;

    public class ProductData {
        // Check generic name first
        @SerializedName("product_name")
        public String productName;

        // Fallback: Check English specific name
        @SerializedName("product_name_en")
        public String productNameEn;

        @SerializedName("brands")
        public String brands;

        // Helper to get the best available name
        public String getBestName() {
            if (productName != null && !productName.isEmpty()) return productName;
            if (productNameEn != null && !productNameEn.isEmpty()) return productNameEn;
            return "Unknown Product";
        }
    }
}