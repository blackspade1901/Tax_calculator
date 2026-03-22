package com.example.taxcalculator.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taxcalculator.R;
import com.example.taxcalculator.models.ProductItem;

import java.util.List;
import java.util.Locale;

/**
 * RecyclerView Adapter for displaying the history of scanned products.
 *
 * Updated to show a richer card per product:
 *   - Product name (truncated if long)
 *   - Brand name
 *   - Barcode (monospace font)
 *   - GST rate badge (e.g. "GST 18%")
 *   - Net price (MRP minus tax)
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<ProductItem> list;

    public HistoryAdapter(List<ProductItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductItem p = list.get(position);

        // Product name
        holder.name.setText(p.getName());

        // Brand — show "Generic" if blank rather than an empty gap
        String brand = p.getBrand();
        holder.brand.setText((brand != null && !brand.isEmpty()) ? brand : "Generic");

        // Barcode — show "N/A" if somehow null
        String barcode = p.getBarcode();
        holder.barcode.setText((barcode != null && !barcode.isEmpty()) ? barcode : "N/A");

        // Net price (MRP minus GST) — what the user actually paid for the product
        holder.price.setText(String.format(Locale.getDefault(),
                "₹ %.2f", p.getNetPrice()));

        // GST rate badge — dynamically shows the actual rate for this product
        // e.g. "GST 0%", "GST 5%", "GST 18%", "GST 40%"
        holder.taxBadge.setText(String.format(Locale.getDefault(),
                "GST %.0f%%", p.getTaxRate()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ViewHolder caches all view references for smooth RecyclerView scrolling.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView brand;
        TextView barcode;
        TextView price;
        TextView taxBadge;

        ViewHolder(View v) {
            super(v);
            name     = v.findViewById(R.id.txtItemName);
            brand    = v.findViewById(R.id.txtItemBrand);
            barcode  = v.findViewById(R.id.txtItemBarcode);
            price    = v.findViewById(R.id.txtItemPrice);
            taxBadge = v.findViewById(R.id.txtItemTaxBadge);
        }
    }
}