package com.example.taxcalculator.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {// Helper to get API for different databases
    public static OpenFoodFactsApi getApi(String type) {
        String baseUrl = "https://world.openfoodfacts.org/";

        // FIX: Check for null to avoid crashes
        if (type == null) {
            type = "food"; // Default fallback
        }

        // FIX: Use "constant".equals(variable) to prevent NullPointerException
        if ("beauty".equals(type)) {
            baseUrl = "https://world.openbeautyfacts.org/";
        } else if ("product".equals(type)) {
            baseUrl = "https://world.openproductsfacts.org/";
        }

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenFoodFactsApi.class);
    }
}
