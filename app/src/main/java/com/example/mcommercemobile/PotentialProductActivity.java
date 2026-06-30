package com.example.mcommercemobile;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapters.PotentialProductAdapter;
import com.example.dals.ReportDAO;
import com.example.models.PotentialProductReport;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PotentialProductActivity
        extends AppCompatActivity {

    private ImageButton btnBackPotentialProduct;
    private ImageButton btnRefreshPotentialProduct;

    private RecyclerView recyclerViewPotentialProduct;

    private TextView txtPotentialSummary;
    private TextView txtEmptyPotentialProduct;

    private ProgressBar progressPotentialProduct;

    private PotentialProductAdapter adapter;

    private boolean isLoading = false;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_potential_product
        );

        addViews();
        addEvents();

        loadPotentialProducts();
    }

    private void addViews() {
        btnBackPotentialProduct =
                findViewById(
                        R.id.btnBackPotentialProduct
                );

        btnRefreshPotentialProduct =
                findViewById(
                        R.id.btnRefreshPotentialProduct
                );

        recyclerViewPotentialProduct =
                findViewById(
                        R.id.recyclerViewPotentialProduct
                );

        txtPotentialSummary =
                findViewById(
                        R.id.txtPotentialSummary
                );

        txtEmptyPotentialProduct =
                findViewById(
                        R.id.txtEmptyPotentialProduct
                );

        progressPotentialProduct =
                findViewById(
                        R.id.progressPotentialProduct
                );

        recyclerViewPotentialProduct.setLayoutManager(
                new LinearLayoutManager(
                        PotentialProductActivity.this
                )
        );

        recyclerViewPotentialProduct.setHasFixedSize(
                true
        );

        adapter = new PotentialProductAdapter();

        recyclerViewPotentialProduct.setAdapter(
                adapter
        );
    }

    private void addEvents() {
        btnBackPotentialProduct.setOnClickListener(
                view -> finish()
        );

        btnRefreshPotentialProduct.setOnClickListener(
                view -> loadPotentialProducts()
        );
    }

    private void loadPotentialProducts() {
        if (isLoading) {
            return;
        }

        setLoadingState(true);

        executorService.execute(() -> {
            try {
                ArrayList<PotentialProductReport> reports =
                        ReportDAO.getTopPotentialProducts(
                                getApplicationContext()
                        );

                runOnUiThread(() -> {
                    adapter.setReports(reports);

                    txtPotentialSummary.setText(
                            "Top "
                                    + reports.size()
                                    + " sản phẩm giảm nhiều nhất "
                                    + "và rating từ 4.0"
                    );

                    boolean isEmpty =
                            reports.isEmpty();

                    txtEmptyPotentialProduct.setVisibility(
                            isEmpty
                                    ? View.VISIBLE
                                    : View.GONE
                    );

                    recyclerViewPotentialProduct.setVisibility(
                            isEmpty
                                    ? View.GONE
                                    : View.VISIBLE
                    );

                    setLoadingState(false);
                });

            } catch (Exception exception) {
                runOnUiThread(() -> {
                    setLoadingState(false);

                    recyclerViewPotentialProduct.setVisibility(
                            View.GONE
                    );

                    txtEmptyPotentialProduct.setVisibility(
                            View.VISIBLE
                    );

                    txtEmptyPotentialProduct.setText(
                            "Không thể tải sản phẩm tiềm năng"
                    );

                    Toast.makeText(
                            PotentialProductActivity.this,
                            "Lỗi báo cáo: "
                                    + exception.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void setLoadingState(
            boolean loading
    ) {
        isLoading = loading;

        progressPotentialProduct.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );

        btnRefreshPotentialProduct.setEnabled(
                !loading
        );
    }

    @Override
    protected void onDestroy() {
        executorService.shutdownNow();

        super.onDestroy();
    }
}