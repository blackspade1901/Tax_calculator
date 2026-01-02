package com.example.taxcalculator.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.taxcalculator.R;
import com.example.taxcalculator.activities.MainActivity;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment responsible for the barcode scanning functionality.
 * Uses CameraX for the camera preview and ML Kit for barcode detection.
 */
@ExperimentalGetImage
public class ScanFragment extends Fragment {

    /**
     * View for displaying the camera preview.
     */
    private PreviewView viewFinder;

    /**
     * Executor for running camera analysis tasks on a background thread.
     */
    private ExecutorService cameraExecutor;

    /**
     * Flag to prevent multiple scans from being processed simultaneously.
     */
    private boolean isScanning = true;

    /**
     * Launcher for requesting camera permission using the modern Activity Result API.
     * Replaces the deprecated onRequestPermissionsResult method.
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Camera permission required", Toast.LENGTH_SHORT).show();
                    }
                    closeFragment();
                }
            });

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object.
     * @param container          The parent view.
     * @param savedInstanceState The saved state.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    /**
     * Called after the view has been created.
     * Initializes the view finder and checks for camera permissions.
     *
     * @param view               The View returned by onCreateView.
     * @param savedInstanceState The saved state.
     */
    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewFinder = view.findViewById(R.id.viewFinder);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> closeFragment());

        // Check and Request Camera Permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            // Use the new Activity Result API to request permission
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * Initializes and binds the CameraX use cases (Preview and ImageAnalysis).
     * Sets up the ML Kit barcode scanner analyzer.
     */
    @androidx.camera.core.ExperimentalGetImage
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // 1. Preview (The visual feed)
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                // 2. Image Analysis (The barcode reader)
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    @androidx.camera.core.ExperimentalGetImage
                    android.media.Image mediaImage = imageProxy.getImage();

                    if (mediaImage != null && isScanning) {
                        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                        BarcodeScanning.getClient().process(image)
                                .addOnSuccessListener(barcodes -> {
                                    for (Barcode barcode : barcodes) {
                                        // FOUND A BARCODE!
                                        if (isScanning) {
                                            isScanning = false; // Stop scanning more frames
                                            String rawValue = barcode.getRawValue();
                                            handleScanResult(rawValue);
                                        }
                                    }
                                })
                                .addOnCompleteListener(task -> imageProxy.close()); // Must close to get next frame
                    } else {
                        imageProxy.close();
                    }
                });

                // 3. Bind to Lifecycle
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                // Handle error
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    /**
     * Processes the scanned barcode result.
     * Delegates the result back to the MainActivity controller.
     *
     * @param code The scanned barcode string.
     */
    private void handleScanResult(String code) {
        // Go back to Main Thread to update UI
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // Pass data back to MainActivity via method call (Controller pattern)
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).onProductScanned(code);
                }
                closeFragment();
            });
        }
    }

    /**
     * Closes the scanning fragment and returns to the previous screen.
     */
    private void closeFragment(){
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
        if (getActivity() != null) {
            getActivity().findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
        }
    }

    /**
     * Cleans up resources when the view is destroyed.
     * Shuts down the background camera executor.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}