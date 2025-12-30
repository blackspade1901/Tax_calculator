package com.example.taxcalculator.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taxcalculator.R;
import com.example.taxcalculator.api.ProductResponse;
import com.example.taxcalculator.api.RetrofitClient;
import com.example.taxcalculator.database.AppDatabase;
import com.example.taxcalculator.fragments.HistoryFragment;
import com.example.taxcalculator.fragments.SettingsFragment;
import com.example.taxcalculator.fragments.ScanFragment;
import com.example.taxcalculator.models.ProductItem;
import com.example.taxcalculator.utils.ThemeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private CardView cardProduct;
    private TextView tvProductName, tvBrandName, tvTaxRate, tvTaxAmount, tvTotalPrice, tvNetPrice;
    private Button btnScan, btnHistory;
    private ImageButton btnSettings;

    private ProductItem selectedProduct;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Database
        db = AppDatabase.getInstance(this);

        // Handle the Back Button the modern way (Android 13+)
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If a fragment is open (Scan or History), close it
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
                } else {
                    // If no fragment is open, close the app normally
                    setEnabled(false); // Disable this custom callback
                    getOnBackPressedDispatcher().onBackPressed(); // Call default behavior
                }
            }
        });

        bindViews();
        setupListeners();
    }

    private void bindViews() {
        cardProduct = findViewById(R.id.productCard);
        tvProductName = findViewById(R.id.txtName);
        tvBrandName = findViewById(R.id.txtBrand);
        tvTotalPrice = findViewById(R.id.txtTotalPrice);
        tvNetPrice = findViewById(R.id.txtNetPrice);
        tvTaxRate = findViewById(R.id.txtTotalRate);
        tvTaxAmount = findViewById(R.id.txtTaxAmount);

        btnSettings = findViewById(R.id.settingBtn);
        btnScan = findViewById(R.id.scanBtn);
        btnHistory = findViewById(R.id.btnHistory);
    }

    private void setupListeners() {
        // SCAN BUTTON: Opens the Camera Fragment
        btnScan.setOnClickListener(v -> {
            ScanFragment fragment = new ScanFragment();
            findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // HISTORY BUTTON: Fetches from DB in Background
        btnHistory.setOnClickListener(v -> {
            new Thread(() -> {
                List<ProductItem> savedList = db.productDao().getAll();

                // Switch back to Main Thread to update UI
                runOnUiThread(() -> {
                    ArrayList<ProductItem> historyList = new ArrayList<>(savedList);
                    HistoryFragment fragment = HistoryFragment.newInstance(historyList);

                    findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit();
                });
            }).start();
        });

        // SETTINGS BUTTON
        btnSettings.setOnClickListener(view -> {
            SettingsFragment fragment = new SettingsFragment();
            fragment.show(getSupportFragmentManager(), "settingsBottomSheet");
        });
    }

    private void updateProductCard() {
        if (selectedProduct == null) {
            cardProduct.setVisibility(View.GONE);
            return;
        }
        cardProduct.setVisibility(View.VISIBLE);

        tvProductName.setText(selectedProduct.getName());
        tvBrandName.setText(selectedProduct.getBrand());
        tvTotalPrice.setText(String.format(Locale.getDefault(), "₹ %.2f", selectedProduct.getTotalPrice()));
        tvTaxRate.setText(String.format(Locale.getDefault(), "%.0f%%(GST)", selectedProduct.getTaxRate()));
        tvTaxAmount.setText(String.format(Locale.getDefault(), "₹ %.2f", selectedProduct.getTaxAmount()));
        tvNetPrice.setText(String.format(Locale.getDefault(), "₹ %.2f", selectedProduct.getNetPrice()));
    }

    // Called by HistoryFragment to clear DB
    public void clearHistory() {
        new Thread(() -> {
            db.productDao().deleteAll();
            runOnUiThread(() -> {
                selectedProduct = null;
                updateProductCard();
                Toast.makeText(this, "History Cleared", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    // --- REAL API LOGIC ---

    // 1. Called by ScanFragment when a barcode is detected
    // 6. WATERFALL SCANNER: Food -> Beauty -> Product -> Manual
    public void onProductScanned(String barcodeValue) {
        Toast.makeText(this, "Searching Food Database...", Toast.LENGTH_SHORT).show();

        // 1. Try FOOD Database
        searchDatabase(barcodeValue, "food", (foundFood, nameFood, brandFood) -> {
            if (foundFood) {
                showPriceDialog(nameFood, brandFood);
                return;
            }

            // 2. Try BEAUTY Database (Soap, Shampoo)
            runOnUiThread(() -> Toast.makeText(this, "Checking Beauty Database...", Toast.LENGTH_SHORT).show());
            searchDatabase(barcodeValue, "beauty", (foundBeauty, nameBeauty, brandBeauty) -> {
                if (foundBeauty) {
                    showPriceDialog(nameBeauty, brandBeauty);
                    return;
                }

                // 3. Try PRODUCTS Database (Tech, Toys)
                runOnUiThread(() -> Toast.makeText(this, "Checking Product Database...", Toast.LENGTH_SHORT).show());
                searchDatabase(barcodeValue, "product", (foundProduct, nameProduct, brandProduct) -> {
                    if (foundProduct) {
                        showPriceDialog(nameProduct, brandProduct);
                    } else {
                        // 4. Not found anywhere? Open Manual Entry
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "New item! Please enter details.", Toast.LENGTH_SHORT).show();
                            showPriceDialog("", ""); // Empty fields
                        });
                    }
                });
            });
        });
    }

    // Helper Interface for callbacks
    interface OnSearchFinished {
        void onResult(boolean found, String name, String brand);
    }

    // Generic Search Helper
    private void searchDatabase(String barcode, String type, OnSearchFinished listener) {
        RetrofitClient.getApi(type).getProduct(barcode).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().status == 1) {
                    String name = response.body().product.getBestName();
                    String brand = response.body().product.brands;
                    // Fix nulls
                    if (name == null) name = "";
                    if (brand == null) brand = "";

                    listener.onResult(true, name, brand);
                } else {
                    listener.onResult(false, null, null);
                }
            }
            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                listener.onResult(false, null, null);
            }
        });
    }

    // Universal Dialog: Handles both API results and Manual Entry
    private void showPriceDialog(String preFilledName, String preFilledBrand) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add Product Details");

        // Layout Container
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // 1. Name Input (Editable)
        final android.widget.EditText inputName = new android.widget.EditText(this);
        inputName.setHint("Product Name");
        inputName.setText(preFilledName.equals("Unknown Product") ? "" : preFilledName); // Leave empty if unknown
        layout.addView(inputName);

        // 2. Brand Input (Editable)
        final android.widget.EditText inputBrand = new android.widget.EditText(this);
        inputBrand.setHint("Brand");
        inputBrand.setText(preFilledBrand.equals("Unknown Brand") ? "" : preFilledBrand);
        layout.addView(inputBrand);

        // 3. Price Input
        final android.widget.EditText inputPrice = new android.widget.EditText(this);
        inputPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputPrice.setHint("MRP (₹)");
        layout.addView(inputPrice);

        // 4. Tax Category Spinner
        final android.widget.Spinner taxSpinner = new android.widget.Spinner(this);

        // GST 2.0 Categories
        String[] categories = {
                "Exempt (0%) - Milk, Bread",
                "Essentials (5%) - Soap, Toothpaste",
                "Standard (18%) - Electronics",
                "Sin/Luxury (40%) - Soda, Tobacco"
        };
        final double[] rates = {0.0, 5.0, 18.0, 40.0};

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        taxSpinner.setAdapter(adapter);

        // Smart Auto-Select Logic
        String currentName = inputName.getText().toString().toLowerCase();
        if (currentName.contains("sprite") || currentName.contains("coke") || currentName.contains("pepsi")) {
            taxSpinner.setSelection(3); // 40%
        } else if (currentName.contains("milk") || currentName.contains("curd")) {
            taxSpinner.setSelection(0); // 0%
        } else {
            taxSpinner.setSelection(1); // Default to 5%
        }

        layout.addView(taxSpinner);
        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = inputName.getText().toString();
            String brand = inputBrand.getText().toString();
            String priceStr = inputPrice.getText().toString();

            if (name.isEmpty()) name = "Unknown Item";
            if (brand.isEmpty()) brand = "Generic";

            if (!priceStr.isEmpty()) {
                double price = Double.parseDouble(priceStr);
                int selectedPos = taxSpinner.getSelectedItemPosition();
                double selectedTax = rates[selectedPos];

                ProductItem newItem = new ProductItem(name, brand, price, selectedTax);

                new Thread(() -> {
                    db.productDao().insert(newItem);
                    runOnUiThread(() -> {
                        selectedProduct = newItem;
                        updateProductCard();
                        Toast.makeText(MainActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                    });
                }).start();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}