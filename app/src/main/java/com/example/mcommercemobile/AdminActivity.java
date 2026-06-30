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
    private Button btnOpenPotentialProducts;
    private Button btnOpenAdminOrders;

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

        btnOpenPotentialProducts = findViewById(
                R.id.btnOpenPotentialProducts
        );

        btnOpenAdminOrders = findViewById(
                R.id.btnOpenAdminOrders
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

        btnOpenPotentialProducts.setOnClickListener(
                view -> {
                    Intent intent = new Intent(
                            AdminActivity.this,
                            PotentialProductActivity.class
                    );

                    startActivity(intent);
                }
        );

        btnOpenAdminOrders.setOnClickListener(
                view -> {
                    Intent intent = new Intent(
                            AdminActivity.this,
                            AdminOrderActivity.class
                    );

                    startActivity(intent);
                }
        );
    }
}