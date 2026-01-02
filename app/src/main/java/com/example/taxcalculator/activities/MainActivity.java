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

import com.example.taxcalculator.R;
import com.example.taxcalculator.fragments.HistoryFragment;
import com.example.taxcalculator.fragments.ProductDialogFragment;
import com.example.taxcalculator.fragments.ScanFragment;
import com.example.taxcalculator.fragments.SettingsFragment;
import com.example.taxcalculator.models.ProductItem;
import com.example.taxcalculator.models.ProductRepository;
import com.example.taxcalculator.utils.ThemeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The main entry point of the application.
 * Handles the primary UI interactions, including product scanning,
 * viewing history, and accessing settings.
 * Acts as the controller coordinating between the View and the Data Repository.
 */
public class MainActivity extends AppCompatActivity implements ProductDialogFragment.ProductDialogListener {

    /**
     * Card view displaying the currently selected or scanned product details.
     */
    private CardView cardProduct;

    /**
     * TextViews for displaying various product attributes.
     */
    private TextView tvProductName, tvBrandName, tvTaxRate, tvTaxAmount, tvTotalPrice, tvNetPrice;

    /**
     * Buttons for triggering scanning and history operations.
     */
    private Button btnScan, btnHistory;

    /**
     * Button for opening the settings menu.
     */
    private ImageButton btnSettings;

    /**
     * The currently displayed product item.
     */
    private ProductItem selectedProduct;

    /**
     * Repository for handling data operations.
     */
    private ProductRepository repository;

    /**
     * Called when the activity is first created.
     * Initializes the theme, sets up the content view, repository, and UI components.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new ProductRepository(getApplication());

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

    /**
     * Binds UI components to their respective XML IDs.
     */
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

    /**
     * Sets up click listeners for the main buttons.
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
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error loading history", Toast.LENGTH_SHORT).show());
                }
            });
        });

        btnSettings.setOnClickListener(view -> {
            SettingsFragment fragment = new SettingsFragment();
            fragment.show(getSupportFragmentManager(), "settingsBottomSheet");
        });
    }

    /**
     * Updates the UI with the details of the selected product.
     * Calculates and displays tax details based on the product's tax category.
     */
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

    /**
     * Clears the product history from the database.
     */
    public void clearHistory() {
        repository.deleteAllProducts(new ProductRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                runOnUiThread(() -> {
                    selectedProduct = null;
                    updateProductCard();
                    Toast.makeText(MainActivity.this, "History Cleared", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error clearing history", Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * Handles the result of a product scan.
     * Initiates a search in the repository and displays the appropriate dialog based on the result.
     *
     * @param barcodeValue The scanned barcode string.
     */
    public void onProductScanned(String barcodeValue) {
        repository.searchProduct(barcodeValue, new ProductRepository.ScanCallback() {
            @Override
            public void onCloudFound(ProductItem item) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Found in Cloud!", Toast.LENGTH_SHORT).show();
                    showProductDialog(item.getName(), item.getBrand(), barcodeValue, item.getPrice(), item.getTaxCategory());
                });
            }

            @Override
            public void onApiFound(String name, String brand, String barcode) {
                runOnUiThread(() -> showProductDialog(name, brand, barcode, 0.0, null));
            }

            @Override
            public void onManualEntryRequired(String barcode) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Not found. Please add details.", Toast.LENGTH_SHORT).show();
                    showProductDialog("", "", barcode, 0.0, null);
                });
            }

            @Override
            public void onBookDetected(String barcode) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Book detected!", Toast.LENGTH_SHORT).show();
                    showProductDialog("", "", barcode, 0.0, null);
                });
            }

            @Override
            public void onSearchStatus(String status) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, status, Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * Displays the product details dialog.
     *
     * @param name       The name of the product (optional).
     * @param brand      The brand of the product (optional).
     * @param barcode    The barcode of the product.
     * @param price      The price of the product (optional).
     * @param categoryId The tax category ID of the product (optional).
     */
    private void showProductDialog(String name, String brand, String barcode, double price, String categoryId) {
        ProductDialogFragment dialog = ProductDialogFragment.newInstance(name, brand, barcode, price, categoryId);
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "ProductDialog");
    }

    /**
     * Callback method invoked when a product is saved from the dialog.
     * Saves the product to the repository and updates the UI.
     *
     * @param item The product item to be saved.
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
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error saving product", Toast.LENGTH_SHORT).show());
            }
        });
    }
}