package com.example.mcommercemobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity
        extends AppCompatActivity {

    private Button btnOpenProduct;
    private Button btnOpenCart;
    private Button btnOpenAdmin;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_main
        );

        addViews();
        addEvents();
    }

    private void addViews() {
        btnOpenProduct = findViewById(
                R.id.btnOpenProduct
        );

        btnOpenCart = findViewById(
                R.id.btnOpenCart
        );

        btnOpenAdmin = findViewById(
                R.id.btnOpenAdmin
        );
    }

    private void addEvents() {
        btnOpenProduct.setOnClickListener(
                view -> {
                    Intent intent = new Intent(
                            MainActivity.this,
                            ProductActivity.class
                    );

                    startActivity(intent);
                }
        );

        btnOpenCart.setOnClickListener(
                view -> {
                    Intent intent = new Intent(
                            MainActivity.this,
                            CartActivity.class
                    );

                    startActivity(intent);
                }
        );

        btnOpenAdmin.setOnClickListener(
                view -> {
                    Intent intent = new Intent(
                            MainActivity.this,
                            AdminActivity.class
                    );

                    startActivity(intent);
                }
        );
    }
}