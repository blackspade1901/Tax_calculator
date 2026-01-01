package com.example.taxcalculator.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

public class ScanFragment extends Fragment {

    private PreviewView viewFinder;
    private ExecutorService cameraExecutor;
    private boolean isScanning = true; // Prevents double-scanning

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewFinder = view.findViewById(R.id.viewFinder);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> closeFragment());

        // Check Permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            // Request Permission
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

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

    private void handleScanResult(String code) {
        // Go back to Main Thread to update UI
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), "Scanned: " + code, Toast.LENGTH_SHORT).show();

            // Pass data back to MainActivity
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onProductScanned(code);
            }
            closeFragment();
        });
    }

    private void closeFragment(){
        getParentFragmentManager().popBackStack();
        // Hide the container again
        requireActivity().findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(getContext(), "Camera permission required", Toast.LENGTH_SHORT).show();
            closeFragment();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
    }
}