package com.example.taxcalculator.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.taxcalculator.R;
import com.example.taxcalculator.models.ProductItem;
import com.example.taxcalculator.utils.TaxManager;

/**
 * A dialog fragment for adding or editing product details.
 * Allows the user to enter product name, brand, price, and select a tax category.
 */
public class ProductDialogFragment extends DialogFragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_BRAND = "brand";
    private static final String ARG_BARCODE = "barcode";
    private static final String ARG_PRICE = "price";
    private static final String ARG_CATEGORY_ID = "category_id";

    /**
     * Listener for communicating dialog events back to the hosting activity or fragment.
     */
    private ProductDialogListener listener;

    /**
     * Interface definition for a callback to be invoked when a product is saved.
     */
    public interface ProductDialogListener {
        void onProductSaved(ProductItem item);
    }

    /**
     * Sets the listener that will receive callbacks from this dialog.
     *
     * @param listener The listener to set.
     */
    public void setListener(ProductDialogListener listener) {
        this.listener = listener;
    }

    /**
     * Creates a new instance of ProductDialogFragment with the given arguments.
     *
     * @param name       The initial product name (can be empty).
     * @param brand      The initial brand name (can be empty).
     * @param barcode    The barcode associated with the product.
     * @param price      The initial price (0.0 if unknown).
     * @param categoryId The ID of the tax category (can be null).
     * @return A new instance of ProductDialogFragment.
     */
    public static ProductDialogFragment newInstance(String name, String brand, String barcode, double price, String categoryId) {
        ProductDialogFragment fragment = new ProductDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_BRAND, brand);
        args.putString(ARG_BARCODE, barcode);
        args.putDouble(ARG_PRICE, price);
        args.putString(ARG_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to build the custom dialog container.
     * Inflates the layout, binds views, populates data, and sets up listeners.
     *
     * @param savedInstanceState The last saved instance state of the Fragment, or null if this is a fresh creation.
     * @return A new Dialog instance to be displayed.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_product, null);
        builder.setView(view);

        String name = getArguments().getString(ARG_NAME, "");
        String brand = getArguments().getString(ARG_BRAND, "");
        String barcode = getArguments().getString(ARG_BARCODE, "");
        double price = getArguments().getDouble(ARG_PRICE, 0.0);
        String categoryId = getArguments().getString(ARG_CATEGORY_ID);

        EditText inputName = view.findViewById(R.id.inputName);
        EditText inputBrand = view.findViewById(R.id.inputBrand);
        EditText inputPrice = view.findViewById(R.id.inputPrice);
        Spinner taxSpinner = view.findViewById(R.id.taxSpinner);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        TextView lblTaxCategory = view.findViewById(R.id.lblTaxCategory);

        inputName.setText(name);
        inputBrand.setText(brand);
        if (price > 0) inputPrice.setText(String.valueOf(price));

        String[] displayCategories = {
                "Exempt (0%) - Milk, Bread",
                "Essentials (5%) - Soap, Toothpaste",
                "Standard (18%) - Electronics",
                "Sin/Luxury (40%) - Soda, Tobacco"
        };
        String[] categoryIds = {
                TaxManager.CAT_EXEMPT,
                TaxManager.CAT_ESSENTIAL,
                TaxManager.CAT_STANDARD,
                TaxManager.CAT_LUXURY
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, displayCategories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taxSpinner.setAdapter(adapter);

        if (categoryId != null) {
            for (int i = 0; i < categoryIds.length; i++) {
                if (categoryIds[i].equals(categoryId)) {
                    taxSpinner.setSelection(i);
                    break;
                }
            }
        } else {
            // Heuristic to guess category based on name keywords
            String currentName = name.toLowerCase();
            if (currentName.contains("sprite") || currentName.contains("coke") || currentName.contains("tobacco")) {
                taxSpinner.setSelection(3);
            } else if (currentName.contains("milk") || currentName.contains("curd")) {
                taxSpinner.setSelection(0);
            } else if (currentName.contains("soap") || currentName.contains("toothpaste")) {
                taxSpinner.setSelection(1);
            } else {
                taxSpinner.setSelection(1);
            }
        }

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnSave.setOnClickListener(v -> {
            String finalName = inputName.getText().toString().isEmpty() ? "Unknown Item" : inputName.getText().toString();
            String finalBrand = inputBrand.getText().toString().isEmpty() ? "Generic" : inputBrand.getText().toString();
            String priceStr = inputPrice.getText().toString();

            if (!priceStr.isEmpty()) {
                double finalPrice = Double.parseDouble(priceStr);
                int selectedPos = taxSpinner.getSelectedItemPosition();
                String selectedCategory = categoryIds[selectedPos];

                ProductItem newItem = new ProductItem(finalName, finalBrand, finalPrice, selectedCategory, barcode);

                if (listener != null) {
                    listener.onProductSaved(newItem);
                }
                dismiss();
            } else {
                inputPrice.setError("Required");
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        lblTaxCategory.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("GST Tax Slabs Guide (2025)")
                    .setMessage("• 0% (Exempt): Fresh vegetables, Milk...\n• 5% (Daily-use): Soaps, Tea...\n• 18% (Standard): Electronics...\n• 40% (Luxury): Tobacco, Soda...")
                    .setPositiveButton("Got it", null)
                    .show();
        });

        return dialog;
    }
}