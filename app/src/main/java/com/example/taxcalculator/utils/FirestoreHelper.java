package com.example.taxcalculator.utils;

import com.example.taxcalculator.models.ProductItem;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Utility class for interacting with Firebase Firestore.
 * Handles reading and writing crowdsourced product data to the cloud database.
 */
public class FirestoreHelper {

    /**
     * The name of the Firestore collection where products are stored.
     */
    private static final String COLLECTION_NAME = "crowdsourced_products";

    /**
     * Interface for defining callbacks for Firestore operations.
     * Mimics the asynchronous response pattern used in networking libraries like Retrofit.
     */
    public interface FirestoreCallback {
        /**
         * Called when a product is successfully retrieved from Firestore.
         * @param item The retrieved ProductItem.
         */
        void onSuccess(ProductItem item);

        /**
         * Called when the product is not found or an error occurs during retrieval.
         */
        void onFailure();
    }

    /**
     * Checks the cloud database for a product with the given barcode.
     * This is the first step in the hybrid scanning process.
     *
     * @param barcode  The barcode string to search for.
     * @param callback The callback to handle the result (success or failure).
     */
    public static void checkProduct(String barcode, FirestoreCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(COLLECTION_NAME).document(barcode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Found it! Convert Firestore document to ProductItem
                        String name = documentSnapshot.getString("name");
                        String brand = documentSnapshot.getString("brand");
                        Double price = documentSnapshot.getDouble("price");
                        String category = documentSnapshot.getString("tax_category");

                        // Safety check to ensure essential fields are present
                        if (name != null && price != null && category != null) {
                            ProductItem item = new ProductItem(
                                    name,
                                    brand != null ? brand : "Generic",
                                    price,
                                    category,
                                    barcode
                            );
                            callback.onSuccess(item);
                        } else {
                            callback.onFailure();
                        }
                    } else {
                        callback.onFailure(); // Document does not exist
                    }
                })
                .addOnFailureListener(e -> callback.onFailure());
    }

    /**
     * Uploads a valid product to the crowdsourced cloud database.
     * This allows other users to benefit from the data entered by the current user.
     * Uses the barcode as the document ID for easy lookup.
     *
     * @param item The ProductItem to be uploaded.
     */
    public static void uploadProduct(ProductItem item) {
        if (item == null || item.getBarcode() == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a map of data to store
        java.util.Map<String, Object> productMap = new java.util.HashMap<>();
        productMap.put("name", item.getName());
        productMap.put("brand", item.getBrand());
        productMap.put("price", item.getPrice());
        productMap.put("tax_category", item.getTaxCategory());
        productMap.put("uploaded_at", com.google.firebase.Timestamp.now());

        // Upload using barcode as ID. SetOptions.merge() ensures we update existing records without overwriting blindly.
        db.collection(COLLECTION_NAME).document(item.getBarcode())
                .set(productMap, com.google.firebase.firestore.SetOptions.merge());
    }
}