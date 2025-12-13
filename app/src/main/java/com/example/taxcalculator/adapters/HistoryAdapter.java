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

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder>{

    private final List<ProductItem> list;

    public HistoryAdapter(List<ProductItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductItem p = list.get(position);
        holder.name.setText(p.getName());
        holder.price.setText("₹"+ p.getNetPrice());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView name, price;

        ViewHolder(View v){
            super(v);
            name = v.findViewById(R.id.txtItemName);
            price = v.findViewById(R.id.txtItemPrice);
        }
    }
}
