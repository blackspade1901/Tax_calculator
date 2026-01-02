package com.example.taxcalculator.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.taxcalculator.R;
import com.example.taxcalculator.utils.ThemeHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * BottomSheetDialogFragment responsible for application settings.
 * Allows the user to toggle dark mode and contact support via email.
 */
public class SettingsFragment extends BottomSheetDialogFragment {

    private SwitchMaterial switchDarkMode;

    /**
     * Creates the dialog and sets up the layout to be transparent.
     * This ensures the custom rounded corners of the bottom sheet background are visible.
     *
     * @param savedInstanceState The saved instance state.
     * @return The Dialog instance.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            // We specifically find the system container named "design_bottom_sheet"
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                // Make the system container transparent so YOUR rounded background shows through
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        });
        return dialog;
    }

    /**
     * Inflates the layout for the settings fragment.
     *
     * @param inflater           The LayoutInflater object.
     * @param container          The parent view.
     * @param savedInstanceState The saved state.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    /**
     * Called after the view is created.
     * Initializes the dark mode switch and support email functionality.
     *
     * @param view               The View returned by onCreateView.
     * @param savedInstanceState The saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchDarkMode = view.findViewById(R.id.switchDarkMode);

        // 1. Load Saved State for Dark Mode
        SharedPreferences prefs = requireContext().getSharedPreferences(ThemeHelper.PREF_NAME, Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(ThemeHelper.KEY_DARk, false);
        switchDarkMode.setChecked(isDark);

        // 2. Handle Dark Mode Toggle
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeHelper.toggleTheme(requireContext(), isChecked);
            ThemeHelper.applyTheme(requireContext());
            // Recreate activity to apply the new theme
            if (getActivity() != null) {
                getActivity().recreate();
            }
            dismiss();
        });

        // Email Support Logic
        LinearLayout llSupportEmail = view.findViewById(R.id.llSupportEmail);
        TextView txtEmail = view.findViewById(R.id.txtEmail);

        llSupportEmail.setOnClickListener(v -> {
            String email = txtEmail.getText().toString();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request");

            try {
                startActivity(Intent.createChooser(intent, "Send Email"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getContext(), "No email app found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Called when the fragment is visible to the user.
     * Sets the dim amount for the window background.
     */
    @Override
    public void onStart() {
        super.onStart();
        // We only use onStart for height and dimming now. Background is handled in onCreateDialog.
        View view = getView();
        if (view != null && getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setDimAmount(0.8f);
        }
    }
}