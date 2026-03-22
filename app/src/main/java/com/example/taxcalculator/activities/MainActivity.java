package com.example.taxcalculator.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.example.taxcalculator.R;
import com.example.taxcalculator.fragments.HistoryFragment;
import com.example.taxcalculator.fragments.ProductDialogFragment;
import com.example.taxcalculator.fragments.ScanFragment;
import com.example.taxcalculator.fragments.SettingsFragment;
import com.example.taxcalculator.models.ProductItem;
import com.example.taxcalculator.models.ProductRepository;
import com.example.taxcalculator.utils.ThemeHelper;
import com.example.taxcalculator.viewmodels.ScanViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The main entry point of the application.
 * Handles primary UI interactions: scanning, history, and settings.
 *
 * Changes in this version:
 *  1. Observes ScanViewModel instead of exposing onProductScanned() publicly.
 *     ScanFragment no longer casts to MainActivity — fully decoupled.
 *  2. Binds CGST / SGST TextViews for the new tax breakdown display.
 */
public class MainActivity extends AppCompatActivity
        implements ProductDialogFragment.ProductDialogListener {

    // ─── Product Card Views ──────────────────────────────────────────────────
    private CardView cardProduct;
    private TextView tvProductName, tvBrandName, tvTaxRate;
    private TextView tvTotalPrice, tvNetPrice, tvTaxAmount;

    // FIX: New CGST / SGST breakdown views
    private TextView tvCGSTLabel, tvCGSTAmount;
    private TextView tvSGSTLabel, tvSGSTAmount;

    // ─── Buttons ─────────────────────────────────────────────────────────────
    private Button btnScan, btnHistory;
    private ImageButton btnSettings;

    // ─── State ───────────────────────────────────────────────────────────────
    private ProductItem selectedProduct;
    private ProductRepository repository;

    // ─── ViewModel ───────────────────────────────────────────────────────────
    // FIX: Shared with ScanFragment to receive scan results without direct coupling.
    private ScanViewModel scanViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository    = new ProductRepository(getApplication());
        scanViewModel = new ViewModelProvider(this).get(ScanViewModel.class);

        setupBackHandler();
        bindViews();
        setupListeners();
        observeScanResults();  // FIX: replaces the public onProductScanned() method
    }

    /**
     * Registers the modern back-press handler.
     */
    private void setupBackHandler() {
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
    }

    /**
     * Binds all UI components to their XML IDs.
     */
    private void bindViews() {
        cardProduct   = findViewById(R.id.productCard);
        tvProductName = findViewById(R.id.txtName);
        tvBrandName   = findViewById(R.id.txtBrand);
        tvTotalPrice  = findViewById(R.id.txtTotalPrice);
        tvNetPrice    = findViewById(R.id.txtNetPrice);
        tvTaxRate     = findViewById(R.id.txtTotalRate);
        tvTaxAmount   = findViewById(R.id.txtTaxAmount);

        // FIX: Bind new CGST / SGST views
        tvCGSTLabel   = findViewById(R.id.txtCGSTLabel);
        tvCGSTAmount  = findViewById(R.id.txtCGSTAmount);
        tvSGSTLabel   = findViewById(R.id.txtSGSTLabel);
        tvSGSTAmount  = findViewById(R.id.txtSGSTAmount);

        btnSettings = findViewById(R.id.settingBtn);
        btnScan     = findViewById(R.id.scanBtn);
        btnHistory  = findViewById(R.id.btnHistory);
    }

    /**
     * Sets up click listeners for all buttons.
     */
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
            repository.getAllProducts(new ProductRepository.DataCallback<List<ProductItem>>() {
                @Override
                public void onSuccess(List<ProductItem> data) {
                    runOnUiThread(() -> {
                        ArrayList<ProductItem> historyList = new ArrayList<>(data);
                        HistoryFragment fragment = HistoryFragment.newInstance(historyList);
                        findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragmentContainer, fragment)
                                .addToBackStack(null)
                                .commit();
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this,
                                    "Error loading history", Toast.LENGTH_SHORT).show()
                    );
                }
            });
        });

        btnSettings.setOnClickListener(view -> {
            SettingsFragment fragment = new SettingsFragment();
            fragment.show(getSupportFragmentManager(), "settingsBottomSheet");
        });
    }

    /**
     * FIX: Observes ScanViewModel for barcode results posted by ScanFragment.
     * Replaces the old public onProductScanned() method that required ScanFragment
     * to cast getActivity() to MainActivity directly.
     *
     * The null check acts as a consumed-event guard — after handling the barcode
     * we call clearScanResult() so the observer doesn't re-fire on rotation.
     */
    private void observeScanResults() {
        scanViewModel.getScannedBarcode().observe(this, barcode -> {
            if (barcode == null) return;  // Already consumed or initial null state
            scanViewModel.clearScanResult();
            handleProductScanned(barcode);
        });
    }

    /**
     * Handles a scanned barcode — searches the repository and shows the product dialog.
     * Previously this was public (onProductScanned) because ScanFragment called it directly.
     * Now it's private — only triggered via the ViewModel observer.
     *
     * @param barcodeValue The scanned barcode string.
     */
    private void handleProductScanned(String barcodeValue) {
        repository.searchProduct(barcodeValue, new ProductRepository.ScanCallback() {
            @Override
            public void onCloudFound(ProductItem item) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Found in Cloud!", Toast.LENGTH_SHORT).show();
                    showProductDialog(item.getName(), item.getBrand(),
                            barcodeValue, item.getPrice(), item.getTaxCategory());
                });
            }

            @Override
            public void onApiFound(String name, String brand, String barcode) {
                runOnUiThread(() ->
                        showProductDialog(name, brand, barcode, 0.0, null)
                );
            }

            @Override
            public void onManualEntryRequired(String barcode) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Not found. Please add details.", Toast.LENGTH_SHORT).show();
                    showProductDialog("", "", barcode, 0.0, null);
                });
            }

            @Override
            public void onBookDetected(String barcode) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Book detected!", Toast.LENGTH_SHORT).show();
                    showProductDialog("", "", barcode, 0.0, null);
                });
            }

            @Override
            public void onSearchStatus(String status) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, status, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /**
     * Updates the product card UI with the selected product's details.
     * FIX: Now also populates CGST and SGST breakdown rows.
     */
    private void updateProductCard() {
        if (selectedProduct == null) {
            cardProduct.setVisibility(View.GONE);
            return;
        }
        cardProduct.setVisibility(View.VISIBLE);

        tvProductName.setText(selectedProduct.getName());
        tvBrandName.setText(selectedProduct.getBrand());
        tvTotalPrice.setText(String.format(Locale.getDefault(),
                "₹ %.2f", selectedProduct.getTotalPrice()));
        tvTaxRate.setText(String.format(Locale.getDefault(),
                "%.0f%% (GST)", selectedProduct.getTaxRate()));

        // Total tax (shown in red)
        tvTaxAmount.setText(String.format(Locale.getDefault(),
                "₹ %.2f", selectedProduct.getTaxAmount()));

        // FIX: CGST breakdown row — label shows the rate, amount shows the value
        tvCGSTLabel.setText(String.format(Locale.getDefault(),
                "CGST (%.0f%%)", selectedProduct.getCGSTRate()));
        tvCGSTAmount.setText(String.format(Locale.getDefault(),
                "₹ %.2f", selectedProduct.getCGST()));

        // FIX: SGST breakdown row
        tvSGSTLabel.setText(String.format(Locale.getDefault(),
                "SGST (%.0f%%)", selectedProduct.getSGSTRate()));
        tvSGSTAmount.setText(String.format(Locale.getDefault(),
                "₹ %.2f", selectedProduct.getSGST()));

        // Net price (actual cost before tax)
        tvNetPrice.setText(String.format(Locale.getDefault(),
                "₹ %.2f", selectedProduct.getNetPrice()));
    }

    /**
     * Shows the product details dialog for editing before saving.
     */
    private void showProductDialog(String name, String brand, String barcode,
                                   double price, String categoryId) {
        ProductDialogFragment dialog = ProductDialogFragment.newInstance(
                name, brand, barcode, price, categoryId
        );
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "ProductDialog");
    }

    /**
     * Callback from ProductDialogFragment when the user taps Save.
     * Inserts the product into the DB and updates the card.
     */
    @Override
    public void onProductSaved(ProductItem item) {
        repository.insertProduct(item, new ProductRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                runOnUiThread(() -> {
                    selectedProduct = item;
                    updateProductCard();
                    Toast.makeText(MainActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                                "Error saving product", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /**
     * Clears all product history from the database.
     * Called from HistoryFragment via the repository — no longer duplicated here.
     */
    public void clearHistory() {
        repository.deleteAllProducts(new ProductRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                runOnUiThread(() -> {
                    selectedProduct = null;
                    updateProductCard();
                    Toast.makeText(MainActivity.this,
                            "History Cleared", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                                "Error clearing history", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}