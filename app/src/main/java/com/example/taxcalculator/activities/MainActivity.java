package com.example.taxcalculator.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taxcalculator.R;
import com.example.taxcalculator.api.ProductResponse;
import com.example.taxcalculator.api.RetrofitClient;
import com.example.taxcalculator.api.UpcItemResponse;
import com.example.taxcalculator.database.AppDatabase;
import com.example.taxcalculator.fragments.HistoryFragment;
import com.example.taxcalculator.fragments.SettingsFragment;
import com.example.taxcalculator.fragments.ScanFragment;
import com.example.taxcalculator.models.ProductItem;
import com.example.taxcalculator.utils.BarcodeRouter;
import com.example.taxcalculator.utils.FirestoreHelper;
import com.example.taxcalculator.utils.ThemeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    // --- CONCURRENCY CONTROLS ---
    private final AtomicBoolean isSearchActive = new AtomicBoolean(false);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final List<Call<ProductResponse>> activeCalls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
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
        btnScan.setOnClickListener(v -> {
            ScanFragment fragment = new ScanFragment();
            findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnHistory.setOnClickListener(v -> {
            new Thread(() -> {
                List<ProductItem> savedList = db.productDao().getAll();
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

    // =================================================================================
    //  HYBRID PARALLEL SCANNER ENGINE
    // =================================================================================

    public void onProductScanned(String barcodeValue) {
        if (isSearchActive.getAndSet(true)) return; // Lock

        // Reset State
        failureCount.set(0);
        activeCalls.clear();

        runOnUiThread(() -> Toast.makeText(this, "Checking Cloud Database...", Toast.LENGTH_SHORT).show());

        // PHASE 1: CHECK FIREBASE FIRST
        FirestoreHelper.checkProduct(barcodeValue, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(ProductItem item) {
                // SUCCESS: Found fully populated item (Price + Tax included)
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Found in Cloud!", Toast.LENGTH_SHORT).show();
                    // Open dialog PRE-FILLED with Price
                    showPriceDialog(item.getName(), item.getBrand(), barcodeValue, item.getPrice(), item.getTaxRate());
                    isSearchActive.set(false); // Unlock
                });
            }

            @Override
            public void onFailure() {
                // FAIL: Not in cloud -> Start Phase 2
                runOnUiThread(() -> startOpenApiRace(barcodeValue));
            }
        });
    }

    // PHASE 2: OPEN API RACE
    private void startOpenApiRace(String barcode) {
        Toast.makeText(this, "Scanning Global APIs...", Toast.LENGTH_SHORT).show();

        // Optimization: Check for Books
        if (BarcodeRouter.getRoute(barcode) == BarcodeRouter.ProductType.BOOK) {
            Toast.makeText(this, "Book detected!", Toast.LENGTH_SHORT).show();
            // Books rarely have MRP in APIs, so we treat as manual entry for now or add Book API later
            triggerManualEntry(barcode);
            return;
        }

        // Fire 3 Requests in Parallel
        checkDatabase(barcode, "food");
        checkDatabase(barcode, "beauty");
        checkDatabase(barcode, "product");
    }

    private void checkDatabase(String barcode, String type) {
        Call<ProductResponse> call = RetrofitClient.getApi(type).getProduct(barcode);
        activeCalls.add(call);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (!isSearchActive.get()) return; // Race already won

                if (response.isSuccessful() && response.body() != null && response.body().status == 1) {
                    // WINNER FOUND
                    if (declareWinner()) {
                        String name = response.body().product.getBestName();
                        String brand = response.body().product.brands;
                        // Open dialog with Name/Brand (Price is 0.0 so user enters it)
                        showPriceDialog(name, brand, barcode, 0.0, 0.0);
                    }
                } else {
                    handleOpenApiFailure(barcode);
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                if (!call.isCanceled()) {
                    handleOpenApiFailure(barcode);
                }
            }
        });
    }

    private void handleOpenApiFailure(String barcode) {
        if (!isSearchActive.get()) return;

        // If all 3 Open APIs fail...
        if (failureCount.incrementAndGet() == 3) {
            // PHASE 3: CHECK BACKUP (UPCitemdb)
            runOnUiThread(() -> Toast.makeText(this, "Checking Backup Database...", Toast.LENGTH_SHORT).show());
            checkBackupDatabase(barcode);
        }
    }

    // PHASE 3: BACKUP API
    private void checkBackupDatabase(String barcode) {
        RetrofitClient.getUpcApi().getProduct(barcode).enqueue(new Callback<UpcItemResponse>() {
            @Override
            public void onResponse(Call<UpcItemResponse> call, Response<UpcItemResponse> response) {
                if (!isSearchActive.get()) return;

                if (response.isSuccessful() && response.body() != null && response.body().total > 0) {
                    isSearchActive.set(false);
                    UpcItemResponse.UpcItem item = response.body().items.get(0);
                    // Open dialog with Name/Brand (Price 0.0)
                    showPriceDialog(item.title, item.brand, barcode, 0.0, 0.0);
                } else {
                    // PHASE 4: MANUAL ENTRY
                    triggerManualEntry(barcode);
                }
            }

            @Override
            public void onFailure(Call<UpcItemResponse> call, Throwable t) {
                triggerManualEntry(barcode);
            }
        });
    }

    // PHASE 4: MANUAL ENTRY
    private void triggerManualEntry(String barcode) {
        if (isSearchActive.getAndSet(false)) {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Not found. Please add details.", Toast.LENGTH_SHORT).show();
                // Open empty dialog
                showPriceDialog("", "", barcode, 0.0, 0.0);
            });
        }
    }

    private boolean declareWinner() {
        boolean won = isSearchActive.getAndSet(false);
        if (won) {
            for (Call<ProductResponse> call : activeCalls) {
                if (!call.isExecuted() && !call.isCanceled()) call.cancel();
            }
            activeCalls.clear();
        }
        return won;
    }

    // =================================================================================
    //  SMART DIALOG (Handles Full, Partial, and Empty Data)
    // =================================================================================

    private void showPriceDialog(String preFilledName, String preFilledBrand, String currentBarcode, double preFilledPrice, double preFilledTax) {
        // 1. Inflate the Custom Layout
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        builder.setView(dialogView);

        // 2. Create the Dialog BEFORE setting listeners (so we can dismiss it later)
        android.app.AlertDialog dialog = builder.create();

        // Make background transparent so our rounded CardView shows properly
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // 3. Bind Views
        android.widget.EditText inputName = dialogView.findViewById(R.id.inputName);
        android.widget.EditText inputBrand = dialogView.findViewById(R.id.inputBrand);
        android.widget.EditText inputPrice = dialogView.findViewById(R.id.inputPrice);
        android.widget.Spinner taxSpinner = dialogView.findViewById(R.id.taxSpinner);
        android.widget.Button btnSave = dialogView.findViewById(R.id.btnSave);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // 4. Pre-fill Data
        if (preFilledName != null && !preFilledName.isEmpty()) inputName.setText(preFilledName);
        if (preFilledBrand != null && !preFilledBrand.isEmpty()) inputBrand.setText(preFilledBrand);
        if (preFilledPrice > 0) inputPrice.setText(String.valueOf(preFilledPrice));

        // 5. Setup Spinner (Tax Rates)
        String[] categories = {
                "Exempt (0%) - Milk, Bread",
                "Essentials (5%) - Soap, Toothpaste",
                "Standard (18%) - Electronics",
                "Sin/Luxury (40%) - Soda, Tobacco"
        };
        final double[] rates = {0.0, 5.0, 18.0, 40.0};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        taxSpinner.setAdapter(adapter);

        // Auto-select Tax Logic
        if (preFilledTax > 0) {
            if (preFilledTax == 5.0) taxSpinner.setSelection(1);
            else if (preFilledTax == 18.0) taxSpinner.setSelection(2);
            else if (preFilledTax == 40.0) taxSpinner.setSelection(3);
            else taxSpinner.setSelection(0);
        } else {
            // Smart Guess based on Name
            String currentName = (preFilledName != null ? preFilledName : "").toLowerCase();
            if (currentName.contains("sprite") || currentName.contains("coke") || currentName.contains("tobacco")) {
                taxSpinner.setSelection(3);
            } else if (currentName.contains("milk") || currentName.contains("curd")) {
                taxSpinner.setSelection(0);
            } else if (currentName.contains("soap") || currentName.contains("toothpaste")) {
                taxSpinner.setSelection(1);
            } else {
                taxSpinner.setSelection(2); // Default to Standard (18%)
            }
        }

        // 6. Button Listeners
        btnSave.setOnClickListener(v -> {
            String name = inputName.getText().toString();
            String brand = inputBrand.getText().toString();
            String priceStr = inputPrice.getText().toString();

            if (name.isEmpty()) name = "Unknown Item";
            if (brand.isEmpty()) brand = "Generic";

            if (!priceStr.isEmpty()) {
                double price = Double.parseDouble(priceStr);
                int selectedPos = taxSpinner.getSelectedItemPosition();
                double selectedTax = rates[selectedPos];

                ProductItem newItem = new ProductItem(name, brand, price, selectedTax, currentBarcode);

                new Thread(() -> {
                    // Save Local
                    db.productDao().insert(newItem);
                    // Upload Cloud
                    com.example.taxcalculator.utils.FirestoreHelper.uploadProduct(newItem);

                    runOnUiThread(() -> {
                        selectedProduct = newItem;
                        updateProductCard();
                        Toast.makeText(MainActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                    });
                }).start();
                dialog.dismiss(); // Close dialog
            } else {
                inputPrice.setError("Required");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}