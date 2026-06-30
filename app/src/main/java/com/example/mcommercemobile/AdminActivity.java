package com.example.mcommercemobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity
        extends AppCompatActivity {

    private ImageButton btnBackAdmin;
    private Button btnOpenPriceAnalysis;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_admin
        );

        addViews();
        addEvents();
    }

    private void addViews() {
        btnBackAdmin = findViewById(
                R.id.btnBackAdmin
        );

        btnOpenPriceAnalysis = findViewById(
                R.id.btnOpenPriceAnalysis
        );
    }

    private void addEvents() {
        btnBackAdmin.setOnClickListener(
                view -> finish()
        );

        btnOpenPriceAnalysis.setOnClickListener(
                view -> {
                    Intent intent = new Intent(
                            AdminActivity.this,
                            PriceAnalysisActivity.class
                    );

                    startActivity(intent);
                }
        );
    }
}