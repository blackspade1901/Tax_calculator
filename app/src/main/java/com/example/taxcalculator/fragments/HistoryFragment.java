package com.example.taxcalculator.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taxcalculator.R;
import com.example.taxcalculator.adapters.HistoryAdapter;
import com.example.taxcalculator.models.ProductItem;
import com.example.taxcalculator.models.ProductRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment responsible for displaying the scan history.
 * Fetches data from the ProductRepository and displays it in a RecyclerView.
 * Also provides functionality to clear the scan history.
 */
public class HistoryFragment extends Fragment {

    private static final String ARG_HISTORY = "history";
    private ProductRepository repository;
    private HistoryAdapter adapter;
    private final ArrayList<ProductItem> historyList = new ArrayList<>();

    /**
     * Required empty public constructor.
     */
    public HistoryFragment() {}

    /**
     * Factory method to create a new instance of this fragment.
     * While data is primarily fetched from the repository, this method allows passing an initial list if needed.
     *
     * @param history The initial list of product items.
     * @return A new instance of HistoryFragment.
     */
    public static HistoryFragment newInstance(ArrayList<ProductItem> history) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_HISTORY, history);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to do initial creation of a fragment.
     * Initializes the ProductRepository.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            repository = new ProductRepository(getActivity().getApplication());
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return Return the View for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned, but before any saved state has been restored in to the view.
     * Sets up the RecyclerView, Adapter, and button listeners.
     *
     * @param view               The View returned by onCreateView().
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerHistory);
        Button btnClear = view.findViewById(R.id.btnClearHistory);

        adapter = new HistoryAdapter(historyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Load data from repository
        loadHistory();

        btnClear.setOnClickListener(v -> clearHistory());
    }

    /**
     * Fetches the full scan history from the repository and updates the UI.
     */
    private void loadHistory() {
        if (repository == null) return;
        
        repository.getAllProducts(new ProductRepository.DataCallback<>() {
            @Override
            public void onSuccess(List<ProductItem> data) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        historyList.clear();
                        historyList.addAll(data);
                        adapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(getActivity(), "Error loading history", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    /**
     * Clears all data from the local database via the repository and updates the UI.
     */
    private void clearHistory() {
        if (repository == null) return;

        repository.deleteAllProducts(new ProductRepository.DataCallback<>() {
            @Override
            public void onSuccess(Void data) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        historyList.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(), "History Cleared", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                 if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(getActivity(), "Error clearing history", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}