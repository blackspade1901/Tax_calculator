package com.example.taxcalculator.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Data model for the response from the UPCitemdb API.
 * Contains information about the search results, including a list of matching items.
 */
public class UpcItemResponse {

    /**
     * Response code string (e.g., "OK").
     */
    @SerializedName("code")
    public String code;

    /**
     * Total number of items found.
     * 0 if no product matches the barcode.
     */
    @SerializedName("total")
    public int total;

    /**
     * List of items returned by the query.
     */
    @SerializedName("items")
    public List<UpcItem> items;

    /**
     * Inner class representing the specific item details returned by UPCitemdb.
     */
    public static class UpcItem {
        /**
         * The title or name of the product.
         */
        @SerializedName("title")
        public String title;

        /**
         * The brand of the product.
         */
        @SerializedName("brand")
        public String brand;

        /**
         * The lowest recorded price for the product.
         */
        @SerializedName("lowest_recorded_price")
        public double price;
    }
}