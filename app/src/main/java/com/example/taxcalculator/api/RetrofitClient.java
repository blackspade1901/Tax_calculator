package com.example.taxcalculator.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton client for creating Retrofit API instances.
 * Manages the connection endpoints for various external product databases.
 */
public class RetrofitClient {

    /**
     * Creates an API instance for the Open Food Facts network (Food, Beauty, or Products).
     * Automatically selects the correct base URL based on the requested type.
     *
     * @param type The type of database to query ("food", "beauty", or "product").
     * @return An implementation of the OpenFoodFactsApi interface.
     */
    public static OpenFoodFactsApi getApi(String type) {
        String baseUrl = "https://world.openfoodfacts.org/"; // Default to Food Facts

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

    /**
     * Creates an API instance for the UPCitemdb service.
     * Used as a fallback when the Open Food Facts network yields no results.
     *
     * @return An implementation of the UpcItemDbApi interface.
     */
    public static UpcItemDbApi getUpcApi() {
        return new Retrofit.Builder()
                .baseUrl("https://api.upcitemdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UpcItemDbApi.class);
    }
}