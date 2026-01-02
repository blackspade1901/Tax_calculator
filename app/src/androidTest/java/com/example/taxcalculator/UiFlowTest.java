package com.example.taxcalculator;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.taxcalculator.activities.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * UI Flow Test for the main screens of the application.
 * Verifies that key UI elements are displayed and interactive.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UiFlowTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testMainActivityElementsDisplayed() {
        // Check if the main scan button is visible
        onView(withId(R.id.scanBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.scanBtn)).check(matches(withText(R.string.scan_product_code)));

        // Check if settings button is visible
        onView(withId(R.id.settingBtn)).check(matches(isDisplayed()));

        // Check if History button is visible
        onView(withId(R.id.btnHistory)).check(matches(isDisplayed()));
        onView(withId(R.id.btnHistory)).check(matches(withText(R.string.view_history)));
    }

    @Test
    public void testNavigateToSettings() {
        // Click on the settings button
        onView(withId(R.id.settingBtn)).perform(click());

        // Check if the settings bottom sheet title is displayed
        onView(withText(R.string.settings_title)).check(matches(isDisplayed()));
        
        // Check if dark mode switch is present
        onView(withId(R.id.switchDarkMode)).check(matches(isDisplayed()));
    }
}