package com.example.taxcalculator.utils;

import com.example.taxcalculator.models.ProductItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class FirestoreHelper {

    private static final String COLLECTION_NAME = "crowdsourced_products";

    // Interface for the callback (like Retrofit's onResponse)
    public interface FirestoreCallback {
        void onSuccess(ProductItem item);
        void onFailure();
    }

    // 1. READ: Check cloud for barcode
    public static void checkProduct(String barcode, FirestoreCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(COLLECTION_NAME).document(barcode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Found it! Convert Firestore document to ProductItem
                        String name = documentSnapshot.getString("name");
                        String brand = documentSnapshot.getString("brand");
                        Double price = documentSnapshot.getDouble("price");
                        Double tax = documentSnapshot.getDouble("tax_rate");

                        // Safety check for nulls
                        if (name != null && price != null && tax != null) {
                            ProductItem item = new ProductItem(
                                    name,
                                    brand != null ? brand : "Generic",
                                    price,
                                    tax,
                                    barcode
                            );
                            callback.onSuccess(item);
                        } else {
                            callback.onFailure();
                        }
                    } else {
                        callback.onFailure(); // Not found in cloud
                    }
                })
                .addOnFailureListener(e -> callback.onFailure());
    }

    // 2. WRITE: Upload valid product to cloud
    public static void uploadProduct(ProductItem item) {
        if (item == null || item.getBarcode() == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a simple map of data
        java.util.Map<String, Object> productMap = new java.util.HashMap<>();
        productMap.put("name", item.getName());
        productMap.put("brand", item.getBrand());
        productMap.put("price", item.getPrice());
        productMap.put("tax_rate", item.getTaxRate());
        productMap.put("uploaded_at", com.google.firebase.Timestamp.now()); // Optional timestamp

        // Upload using barcode as ID (Merge = true means update if exists)
        db.collection(COLLECTION_NAME).document(item.getBarcode())
                .set(productMap, com.google.firebase.firestore.SetOptions.merge());
    }
}