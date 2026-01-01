package com.example.taxcalculator.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taxcalculator.R;

public class WelcomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom);

        Button getstartedButton = findViewById(R.id.getStartedBtn);

        getstartedButton.setOnClickListener(view -> {
            Intent intent  = new Intent(WelcomActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

    }
}