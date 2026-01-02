package com.example.taxcalculator.models;

import android.app.Application;

import com.example.taxcalculator.api.ProductResponse;
import com.example.taxcalculator.api.RetrofitClient;
import com.example.taxcalculator.api.UpcItemResponse;
import com.example.taxcalculator.database.AppDatabase;
import com.example.taxcalculator.database.ProductDao;
import com.example.taxcalculator.utils.BarcodeRouter;
import com.example.taxcalculator.utils.FirestoreHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository class responsible for managing data operations.
 * It coordinates interactions between local database, cloud storage, and external APIs.
 * This class abstracts the data source implementation from the rest of the application.
 */
public class ProductRepository {

    private final ProductDao productDao;
    private final ExecutorService executorService;
    private final AtomicBoolean isSearchActive = new AtomicBoolean(false);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final List<Call<ProductResponse>> activeCalls = new ArrayList<>();

    /**
     * Interface for handling asynchronous data operations.
     * @param <T> The type of the result data.
     */
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onFailure(String error);
    }

    /**
     * Interface for handling product scanning results.
     * defines callbacks for different outcomes of a product search.
     */
    public interface ScanCallback {
        void onCloudFound(ProductItem item);
        void onApiFound(String name, String brand, String barcode);
        void onManualEntryRequired(String barcode);
        void onBookDetected(String barcode);
        void onSearchStatus(String status);
    }

    /**
     * Initializes the repository with the application context.
     * Sets up the database access object and the background executor service.
     *
     * @param application The application context.
     */
    public ProductRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        productDao = db.productDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Retrieves all products from the local database asynchronously.
     *
     * @param callback Callback to handle the list of products or an error.
     */
    public void getAllProducts(DataCallback<List<ProductItem>> callback) {
        executorService.execute(() -> {
            List<ProductItem> data = productDao.getAll();
            callback.onSuccess(data);
        });
    }

    /**
     * Inserts a product into the local database and uploads it to the cloud.
     *
     * @param item     The product item to be inserted.
     * @param callback Callback to indicate completion.
     */
    public void insertProduct(ProductItem item, DataCallback<Void> callback) {
        executorService.execute(() -> {
            productDao.insert(item);
            FirestoreHelper.uploadProduct(item);
            callback.onSuccess(null);
        });
    }

    /**
     * Deletes all products from the local database.
     *
     * @param callback Callback to indicate completion.
     */
    public void deleteAllProducts(DataCallback<Void> callback) {
        executorService.execute(() -> {
            productDao.deleteAll();
            callback.onSuccess(null);
        });
    }

    /**
     * Searches for a product using its barcode.
     * First checks the cloud database, and if not found, initiates a search across external APIs.
     *
     * @param barcodeValue The barcode of the product to search for.
     * @param callback     Callback to handle the search results.
     */
    public void searchProduct(String barcodeValue, ScanCallback callback) {
        if (isSearchActive.getAndSet(true)) return;

        failureCount.set(0);
        activeCalls.clear();
        callback.onSearchStatus("Identifying product...");

        // Phase 1: Cloud
        FirestoreHelper.checkProduct(barcodeValue, new FirestoreHelper.FirestoreCallback() {
            @Override
            public void onSuccess(ProductItem item) {
                isSearchActive.set(false);
                callback.onCloudFound(item);
            }

            @Override
            public void onFailure() {
                startOpenApiRace(barcodeValue, callback);
            }
        });
    }

    /**
     * Initiates parallel API calls to external services if the product is not found in the cloud.
     *
     * @param barcode  The barcode to search for.
     * @param callback Callback to report search progress and results.
     */
    private void startOpenApiRace(String barcode, ScanCallback callback) {
        callback.onSearchStatus("Searching global databases...");

        if (BarcodeRouter.getRoute(barcode) == BarcodeRouter.ProductType.BOOK) {
            callback.onBookDetected(barcode);
            isSearchActive.set(false);
            return;
        }

        checkApi(barcode, "food", callback);
        checkApi(barcode, "beauty", callback);
        checkApi(barcode, "product", callback);
    }

    /**
     * Helper method to call a specific API endpoint.
     *
     * @param barcode  The barcode to search for.
     * @param type     The category type for the API call.
     * @param callback Callback to report findings.
     */
    private void checkApi(String barcode, String type, ScanCallback callback) {
        Call<ProductResponse> call = RetrofitClient.getApi(type).getProduct(barcode);
        activeCalls.add(call);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (!isSearchActive.get()) return;

                if (response.isSuccessful() && response.body() != null && response.body().status == 1) {
                    if (declareWinner()) {
                        String name = response.body().product.getBestName();
                        String brand = response.body().product.brands;
                        callback.onApiFound(name, brand, barcode);
                    }
                } else {
                    handleApiFailure(barcode, callback);
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                if (!call.isCanceled()) {
                    handleApiFailure(barcode, callback);
                }
            }
        });
    }

    /**
     * Handles failed API attempts.
     * If all primary APIs fail, triggers a backup database check.
     *
     * @param barcode  The barcode being searched.
     * @param callback Callback to report status.
     */
    private void handleApiFailure(String barcode, ScanCallback callback) {
        if (!isSearchActive.get()) return;

        if (failureCount.incrementAndGet() == 3) {
            callback.onSearchStatus("Performing deep lookup...");
            checkBackupDatabase(barcode, callback);
        }
    }

    /**
     * Checks the backup UPC database as a fallback mechanism.
     *
     * @param barcode  The barcode to search for.
     * @param callback Callback to report findings.
     */
    private void checkBackupDatabase(String barcode, ScanCallback callback) {
        RetrofitClient.getUpcApi().getProduct(barcode).enqueue(new Callback<UpcItemResponse>() {
            @Override
            public void onResponse(Call<UpcItemResponse> call, Response<UpcItemResponse> response) {
                if (!isSearchActive.get()) return;

                if (response.isSuccessful() && response.body() != null && response.body().total > 0) {
                    isSearchActive.set(false);
                    UpcItemResponse.UpcItem item = response.body().items.get(0);
                    callback.onApiFound(item.title, item.brand, barcode);
                } else {
                    triggerManualEntry(barcode, callback);
                }
            }

            @Override
            public void onFailure(Call<UpcItemResponse> call, Throwable t) {
                triggerManualEntry(barcode, callback);
            }
        });
    }

    /**
     * Signals that manual entry is required when all automated searches fail.
     *
     * @param barcode  The barcode being processed.
     * @param callback Callback to notify the UI.
     */
    private void triggerManualEntry(String barcode, ScanCallback callback) {
        if (isSearchActive.getAndSet(false)) {
            callback.onManualEntryRequired(barcode);
        }
    }

    /**
     * Atomically determines if the current API response is the first successful one.
     * Cancels other pending API calls if a winner is declared.
     *
     * @return True if this is the winning call, false otherwise.
     */
    private boolean declareWinner() {
        boolean won = isSearchActive.getAndSet(false);
        if (won) {
            for (Call<ProductResponse> call : activeCalls) {
                if (!call.isExecuted() && !call.isCanceled()) call.cancel();
            }
            activeCalls.clear();
        }
        return won;
    }
}