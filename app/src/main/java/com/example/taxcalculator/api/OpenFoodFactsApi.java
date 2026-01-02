package com.example.taxcalculator.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Retrofit interface for accessing the Open Food Facts API (and related Open Beauty/Product Facts).
 * Defines the endpoints for retrieving product information based on barcodes.
 */
public interface OpenFoodFactsApi {

    /**
     * Retrieves product details for a given barcode.
     * The {barcode} path parameter is replaced by the actual barcode string during the request.
     *
     * @param barcode The barcode of the product to search for.
     * @return A Call object representing the asynchronous API request, returning a ProductResponse.
     */
    @GET("api/v0/product/{barcode}.json")
    Call<ProductResponse> getProduct(@Path("barcode") String barcode);
}