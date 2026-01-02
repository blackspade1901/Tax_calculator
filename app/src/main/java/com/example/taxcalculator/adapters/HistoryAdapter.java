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

/**
 * RecyclerView Adapter for displaying the history of scanned products.
 * Binds product data to the view items in the history list.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    /**
     * List of products to be displayed.
     */
    private final List<ProductItem> list;

    /**
     * Constructs a new HistoryAdapter.
     *
     * @param list The list of ProductItem objects to display.
     */
    public HistoryAdapter(List<ProductItem> list) {
        this.list = list;
    }

    /**
     * Called when the RecyclerView needs a new ViewHolder of the given type to represent an item.
     * Inflates the layout for a single history item.
     *
     * @param parent   The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds the View for the history item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(v);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * Updates the contents of the ViewHolder to reflect the item at the given position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductItem p = list.get(position);
        holder.name.setText(p.getName());
        // Display the net price formatted as currency
        holder.price.setText(String.format(java.util.Locale.getDefault(), "₹ %.2f", p.getNetPrice()));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in the list.
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ViewHolder class for caching view references for each history item.
     * Prevents repeated calls to findViewById during scrolling.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price;

        /**
         * Constructor for the ViewHolder.
         *
         * @param v The view for the history item.
         */
        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.txtItemName);
            price = v.findViewById(R.id.txtItemPrice);
        }
    }
}