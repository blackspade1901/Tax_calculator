package com.example.taxcalculator.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taxcalculator.R;
import com.example.taxcalculator.database.AppDatabase;
import com.example.taxcalculator.fragments.HistoryFragment;
import com.example.taxcalculator.fragments.SettingsFragment;
import com.example.taxcalculator.models.ProductItem;
import com.example.taxcalculator.utils.ThemeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private CardView cardProduct;
    private TextView tvProductName, tvBrandName, tvTaxRate, tvTaxAmount,  tvTotalPrice, tvNetPrice;
    private Button btnScan, btnHistory;
    private ImageButton btnSettings;

    private ProductItem selectedProduct;
    private final List<ProductItem> sample = new ArrayList<>();
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);
        initSamples();
        bindViews();
        setupListeners();
    }

    private void initSamples(){
        sample.add(new ProductItem("Amul Milk", "Amul", 30, 18.0));
        sample.add(new ProductItem("Packaged wheat flour 5kg", "Aashirvaad", 260,5.0));
        sample.add(new ProductItem("Packaged fruit juice 1l",	"Real", 120, 12.0));
    }

    private void bindViews(){
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

    private void setupListeners(){
        btnScan.setOnClickListener(v -> simulateScan());

        btnHistory.setOnClickListener(v -> {

            List<ProductItem> savedList = db.productDao().getAll();
            ArrayList<ProductItem> historyList = new ArrayList<>(savedList);
            HistoryFragment fragment =
                    HistoryFragment.newInstance(new ArrayList<>(historyList));

            findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });



        btnSettings.setOnClickListener(view -> {
            SettingsFragment fragment = new SettingsFragment();
            fragment.show(getSupportFragmentManager(), "settingsBottomSheet");
        });
    }

    private void simulateScan(){
        int idx = (int) (Math.random() * sample.size());
        ProductItem next = sample.get(idx);

        ProductItem item = new ProductItem(next.getName(), next.getBrand(), next.getTotalPrice(), next.getTaxRate());

        db.productDao().insert(item);
        selectedProduct = item;
        updateProductCard();
        Toast.makeText(this, "Saved to History", Toast.LENGTH_SHORT).show();
    }

    private void updateProductCard(){
        if(selectedProduct == null){
            cardProduct.setVisibility(View.GONE);
            return;
        }
        cardProduct.setVisibility(View.VISIBLE);

        tvProductName.setText(selectedProduct.getName());
        tvBrandName.setText(selectedProduct.getBrand());
        tvTotalPrice.setText(String.format("₹ %.2f", selectedProduct.getTotalPrice()));
        tvTaxRate.setText(String.format(Locale.getDefault(), "%.0f%%(GST)", selectedProduct.getTaxRate()));
        tvTaxAmount.setText(String.format(Locale.getDefault(), "₹ %.2f", selectedProduct.getTaxAmount()));
        tvNetPrice.setText(String.format(Locale.getDefault(), "₹ %.2f", selectedProduct.getNetPrice()));
    }

    public void clearHistory(){
        db.productDao().deleteAll();
        selectedProduct = null;
        updateProductCard();

        Toast.makeText(this, "History Cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }
}
