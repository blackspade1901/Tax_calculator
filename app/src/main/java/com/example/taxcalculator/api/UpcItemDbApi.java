package com.example.taxcalculator.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit interface for the UPCitemdb API.
 * Defines the endpoint used to look up products by their UPC/EAN barcode
 * as a fallback when other APIs fail.
 */
public interface UpcItemDbApi {

    /**
     * Looks up a product by its UPC barcode.
     * Note: This is a trial endpoint and may have rate limits.
     *
     * @param barcode The UPC/EAN barcode string.
     * @return A Call object for the asynchronous request, returning a UpcItemResponse.
     */
    @GET("prod/trial/lookup")
    Call<UpcItemResponse> getProduct(@Query("upc") String barcode);
}