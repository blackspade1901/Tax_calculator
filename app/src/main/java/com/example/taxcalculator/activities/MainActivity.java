package com.example.taxcalculator.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.taxcalculator.R;
import com.example.taxcalculator.models.ProductItem;
import com.example.taxcalculator.utils.ThemeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LinearLayout cardProduct;
    private TextView tvProductName, tvBrandName, tvTaxRate, tvTaxAmount,  tvTotalPrice, tvNetPrice;
    private Button btnScan, btnHistory;
    private ImageButton btnSettings;

    private ProductItem selectedProduct;
    private final List<ProductItem> history = new ArrayList<>();
    private final List<ProductItem> sample = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            // history implementation
        });

        btnSettings.setOnClickListener(view -> {
            // settings implementation
        });
    }

    private void simulateScan(){
        int idx = history.size() % sample.size();
        ProductItem next = sample.get(idx);

        ProductItem item = new ProductItem(next.getName(), next.getBrand(), next.getTotalPrice(), next.getTaxRate());

        selectedProduct = item;
        history.add(0, item);
        runOnUiThread(this::updateProductCard);
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
        history.clear();
        selectedProduct = null;
        updateProductCard();
    }

    public List<ProductItem> getHistory(){
        return history;
    }
}
