package com.example.taxcalculator.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton manager class for handling tax rates and categories.
 * Provides access to standard tax rates (Exempt, Essential, Standard, Luxury)
 * and allows for dynamic updates of these rates from a remote source (e.g., Cloud).
 */
public class TaxManager {

    /**
     * Singleton instance of the TaxManager.
     */
    private static TaxManager instance;

    /**
     * Map storing the tax rates, where the key is the category ID and the value is the tax percentage.
     */
    private Map<String, Double> taxRates;

    /**
     * Category ID for exempt items (0% tax).
     * Examples: Milk, Bread, Fresh Vegetables.
     */
    public static final String CAT_EXEMPT = "exempt";

    /**
     * Category ID for essential items (5% tax).
     * Examples: Soap, Oil, Medicines.
     */
    public static final String CAT_ESSENTIAL = "essential";

    /**
     * Category ID for standard items (18% tax).
     * Examples: Electronics, Home Appliances.
     */
    public static final String CAT_STANDARD = "standard";

    /**
     * Category ID for luxury or sin goods (40% tax).
     * Examples: Tobacco, Aerated Drinks.
     */
    public static final String CAT_LUXURY = "luxury";

    /**
     * Private constructor to enforce the Singleton pattern.
     * Initializes the default tax rates.
     */
    private TaxManager() {
        taxRates = new HashMap<>();
        taxRates.put(CAT_EXEMPT, 0.0);
        taxRates.put(CAT_ESSENTIAL, 5.0);
        taxRates.put(CAT_STANDARD, 18.0);
        taxRates.put(CAT_LUXURY, 40.0);
    }

    /**
     * Retrieves the singleton instance of TaxManager.
     *
     * @return The single instance of TaxManager.
     */
    public static synchronized TaxManager getInstance() {
        if (instance == null) {
            instance = new TaxManager();
        }
        return instance;
    }

    /**
     * Retrieves the tax rate for a specific category ID.
     *
     * @param categoryId The ID of the category (e.g., CAT_STANDARD).
     * @return The tax rate as a percentage. Returns 5.0 (Essential) if the category is unknown or null.
     */
    public double getRate(String categoryId) {
        if (categoryId == null) return 5.0; // Default to Essentials
        Double rate = taxRates.get(categoryId);
        return rate != null ? rate : 5.0;
    }

    /**
     * Updates the local tax rates with values fetched from a remote source (e.g., Firebase).
     * Handles potential type mismatches (Double vs Long) from Firestore.
     *
     * @param cloudData A map containing the new rates from the cloud.
     */
    public void updateRates(Map<String, Object> cloudData) {
        if (cloudData == null) return;

        for (String key : cloudData.keySet()) {
            Object val = cloudData.get(key);

            if (val instanceof Double) {
                taxRates.put(key, (Double) val);
            } else if (val instanceof Long) {
                // Firestore sometimes sends Long instead of Double for integer-like numbers
                taxRates.put(key, ((Long) val).doubleValue());
            }
        }
    }
}