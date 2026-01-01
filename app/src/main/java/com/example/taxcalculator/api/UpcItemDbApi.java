package com.example.taxcalculator.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UpcItemDbApi {
    @GET("prod/trial/lookup")
    Call<UpcItemResponse> getProduct(@Query("upc") String barcode);
}