package com.example.taxcalculator.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel shared between MainActivity and ScanFragment.
 *
 * Purpose: Decouples the scan result communication so that ScanFragment
 * no longer needs to cast getActivity() to MainActivity directly.
 *
 * How it works:
 *   1. ScanFragment posts the scanned barcode to scannedBarcode LiveData.
 *   2. MainActivity observes scannedBarcode and calls onProductScanned().
 *   3. After consuming the event, MainActivity calls clearScanResult()
 *      to reset the LiveData so it doesn't re-fire on config changes.
 *
 * This pattern is called a "SingleLiveEvent" workaround — the null check
 * in MainActivity's observer acts as a consumed-event gate.
 */
public class ScanViewModel extends ViewModel {

    private final MutableLiveData<String> scannedBarcode = new MutableLiveData<>();

    /**
     * Posts a new scanned barcode value.
     * Called by ScanFragment after a successful barcode detection.
     *
     * @param barcode The raw barcode string from ML Kit.
     */
    public void postScanResult(String barcode) {
        scannedBarcode.postValue(barcode);
    }

    /**
     * Clears the scan result after it has been consumed.
     * Must be called by the observer (MainActivity) immediately after handling
     * the barcode to prevent re-delivery on rotation or fragment re-attach.
     */
    public void clearScanResult() {
        scannedBarcode.setValue(null);
    }

    /**
     * Returns the LiveData that emits scanned barcodes.
     *
     * @return LiveData<String> observed by MainActivity.
     */
    public LiveData<String> getScannedBarcode() {
        return scannedBarcode;
    }
}