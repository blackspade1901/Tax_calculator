package com.example.taxcalculator.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Utility class for managing the application's theme (Dark Mode / Light Mode).
 * Persists user preference using SharedPreferences.
 */
public class ThemeHelper {

    /**
     * Name of the SharedPreferences file.
     */
    public static final String PREF_NAME = "Tax_Calc";

    /**
     * Key used to store the dark mode preference boolean.
     */
    public static final String KEY_DARk = "dark_mode";


    /**
     * Applies the saved theme preference to the application.
     * Should be called in the onCreate() method of activities, or in the Application class.
     *
     * @param context The context used to access SharedPreferences.
     */
    public static void applyTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARk, false);

        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    /**
     * Toggles the theme preference and saves it to SharedPreferences.
     * Note: This does not automatically recreate the activity. The caller must handle activity recreation.
     *
     * @param context The context used to access SharedPreferences.
     * @param dark    True to enable dark mode, false for light mode.
     */
    public static void toggleTheme(Context context, boolean dark) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DARk, dark).apply();
    }
}