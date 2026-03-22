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
import androidx.lifecycle.ViewModelProvider;

import com.example.taxcalculator.R;
import com.example.taxcalculator.viewmodels.ScanViewModel;
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
 *
 * FIX: Decoupled from MainActivity.
 * Previously used ((MainActivity) getActivity()).onProductScanned(code) which was
 * a direct cast — meaning ScanFragment could only ever live inside MainActivity.
 * Now uses a shared ScanViewModel. ScanFragment posts the result; MainActivity observes it.
 * Neither class references the other directly.
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
     * Shared ViewModel for posting scan results to the host activity.
     * FIX: Replaces the direct MainActivity cast.
     */
    private ScanViewModel scanViewModel;

    /**
     * Launcher for requesting camera permission using the modern Activity Result API.
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Camera permission required",
                                Toast.LENGTH_SHORT).show();
                    }
                    closeFragment();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewFinder = view.findViewById(R.id.viewFinder);
        view.findViewById(R.id.btnClose).setOnClickListener(v -> closeFragment());

        // FIX: Obtain the ScanViewModel scoped to the parent Activity.
        // Using requireActivity() as the ViewModelStoreOwner ensures MainActivity
        // and ScanFragment share the exact same ViewModel instance.
        scanViewModel = new ViewModelProvider(requireActivity()).get(ScanViewModel.class);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * Initializes and binds the CameraX use cases (Preview and ImageAnalysis).
     */
    @androidx.camera.core.ExperimentalGetImage
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    @androidx.camera.core.ExperimentalGetImage
                    android.media.Image mediaImage = imageProxy.getImage();

                    if (mediaImage != null && isScanning) {
                        InputImage image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.getImageInfo().getRotationDegrees()
                        );

                        BarcodeScanning.getClient().process(image)
                                .addOnSuccessListener(barcodes -> {
                                    for (Barcode barcode : barcodes) {
                                        if (isScanning) {
                                            isScanning = false;
                                            handleScanResult(barcode.getRawValue());
                                        }
                                    }
                                })
                                .addOnCompleteListener(task -> imageProxy.close());
                    } else {
                        imageProxy.close();
                    }
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis
                );

            } catch (ExecutionException | InterruptedException e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to start camera",
                            Toast.LENGTH_SHORT).show();
                }
                closeFragment();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    /**
     * Processes the scanned barcode result.
     * FIX: Posts result via ViewModel instead of casting to MainActivity.
     *
     * @param code The scanned barcode string.
     */
    private void handleScanResult(String code) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // FIX: Post to ViewModel. MainActivity's observer handles the rest.
                // No cast to MainActivity needed — ScanFragment is now fully reusable.
                scanViewModel.postScanResult(code);
                closeFragment();
            });
        }
    }

    /**
     * Closes the scanning fragment and returns to the previous screen.
     */
    private void closeFragment() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
        if (getActivity() != null) {
            getActivity().findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
        }
    }

    /**
     * Cleans up the background camera executor when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}