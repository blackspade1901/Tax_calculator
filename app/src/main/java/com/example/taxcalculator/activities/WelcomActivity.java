package com.example.taxcalculator.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taxcalculator.R;
import com.example.taxcalculator.utils.ThemeHelper;

/**
 * The introductory activity for the application.
 * Displays a welcome screen on the very first launch only.
 * On subsequent launches, it redirects immediately to MainActivity.
 *
 * Note: Class name retains original "WelcomActivity" spelling (missing 'e') intentionally
 * to avoid breaking the AndroidManifest.xml entry and any deep links. Rename carefully
 * using Android Studio's refactor tool if you want to fix the typo.
 */
public class WelcomActivity extends AppCompatActivity {

    // FIX: Key to track whether the user has seen the welcome screen before.
    // Stored in the same SharedPreferences file as theme settings for simplicity.
    private static final String KEY_FIRST_LAUNCH = "is_first_launch";

    /**
     * Called when the activity is first created.
     * Checks if this is the first launch. If not, skips directly to MainActivity.
     *
     * @param savedInstanceState Bundle with saved state, or null on fresh create.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FIX: Check first-launch flag BEFORE setContentView to avoid
        // briefly flashing the welcome screen on returning users.
        SharedPreferences prefs = getSharedPreferences(
                ThemeHelper.PREF_NAME, MODE_PRIVATE
        );

        if (!prefs.getBoolean(KEY_FIRST_LAUNCH, true)) {
            // Not a first launch — skip welcome screen entirely
            goToMain();
            return;
        }

        // First launch — show the welcome screen and mark as seen
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();

        setContentView(R.layout.activity_welcom);

        Button getStartedButton = findViewById(R.id.getStartedBtn);
        getStartedButton.setOnClickListener(view -> goToMain());
    }

    /**
     * Navigates to MainActivity and removes WelcomActivity from the back stack.
     * Calling finish() ensures pressing Back from MainActivity doesn't return here.
     */
    private void goToMain() {
        Intent intent = new Intent(WelcomActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}