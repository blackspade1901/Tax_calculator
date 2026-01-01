package com.example.taxcalculator.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taxcalculator.R;
import com.example.taxcalculator.activities.MainActivity;
import com.example.taxcalculator.adapters.HistoryAdapter;
import com.example.taxcalculator.models.ProductItem;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    private static final String ARG_HISTORY = "history";
    private ArrayList<ProductItem> history;

    public HistoryFragment() {} // REQUIRED empty constructor

    public static HistoryFragment newInstance(ArrayList<ProductItem> history) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_HISTORY, history);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        if (getArguments() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                history = (ArrayList<ProductItem>) getArguments().getSerializable(ARG_HISTORY, ArrayList.class);
            }
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerHistory);
        Button btnClear = view.findViewById(R.id.btnClearHistory);

        HistoryAdapter adapter = new HistoryAdapter(history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        btnClear.setOnClickListener(v -> {
            history.clear();
            adapter.notifyDataSetChanged();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).clearHistory();
            }
        });

        return view;
    }
}

