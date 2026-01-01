package com.example.taxcalculator.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // 1. Helper for Open Food / Beauty / Product Facts
    public static OpenFoodFactsApi getApi(String type) {
        String baseUrl = "https://world.openfoodfacts.org/"; // Default

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

    // 2. MISSING METHOD: Helper for UPCitemdb
    public static UpcItemDbApi getUpcApi() {
        return new Retrofit.Builder()
                .baseUrl("https://api.upcitemdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UpcItemDbApi.class);
    }
}