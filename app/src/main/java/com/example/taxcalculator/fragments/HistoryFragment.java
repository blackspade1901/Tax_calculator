package com.example.taxcalculator.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.taxcalculator.R;
import com.example.taxcalculator.activities.MainActivity;
import com.example.taxcalculator.adapters.HistoryAdapter;
import com.example.taxcalculator.models.ProductItem;

import java.util.List;

public class HistoryFragment extends Fragment {

    private List<ProductItem> history;

    public HistoryFragment(List<ProductItem> history){
        this.history = history;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerHistory);
        Button btnClear = view.findViewById(R.id.btnClearHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        HistoryAdapter adapter = new HistoryAdapter(history);
        recyclerView.setAdapter(adapter);

        btnClear.setOnClickListener(v -> {
            history.clear();
            adapter.notifyDataSetChanged();

            if(getActivity() instanceof MainActivity){
                ((MainActivity) getActivity()).clearHistory();
            }
        });
        return view;
    }
}