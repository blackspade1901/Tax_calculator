package com.example.taxcalculator.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UpcItemResponse {
    @SerializedName("code")
    public String code; // "OK" or error code

    @SerializedName("total")
    public int total;   // 0 means not found

    @SerializedName("items")
    public List<UpcItem> items;

    // Inner class to hold the item details
    public static class UpcItem {
        @SerializedName("title")
        public String title;

        @SerializedName("brand")
        public String brand;

        @SerializedName("lowest_recorded_price")
        public double price;
    }
}