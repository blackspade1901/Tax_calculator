package com.example.taxcalculator.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taxcalculator.R;

/**
 * The introductory activity for the application.
 * Displays a welcome screen and provides a button to navigate to the main activity.
 * This activity is typically shown only once or as the launch screen.
 */
public class WelcomActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * Sets up the UI layout and initializes the "Get Started" button listener.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom);

        Button getStartedButton = findViewById(R.id.getStartedBtn);

        getStartedButton.setOnClickListener(view -> {
            Intent intent = new Intent(WelcomActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}