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

import com.example.adapters.PriceAnalysisAdapter;
import com.example.dals.ReportDAO;
import com.example.models.CategoryPriceReport;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PriceAnalysisActivity
        extends AppCompatActivity {

    private ImageButton btnBackPriceReport;
    private ImageButton btnRefreshPriceReport;

    private RecyclerView recyclerViewPriceReport;

    private TextView txtReportSummary;
    private TextView txtEmptyPriceReport;

    private ProgressBar progressPriceReport;

    private PriceAnalysisAdapter adapter;

    private boolean isLoading = false;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_price_analysis
        );

        addViews();
        addEvents();

        loadPriceAnalysis();
    }

    private void addViews() {
        btnBackPriceReport =
                findViewById(
                        R.id.btnBackPriceReport
                );

        btnRefreshPriceReport =
                findViewById(
                        R.id.btnRefreshPriceReport
                );

        recyclerViewPriceReport =
                findViewById(
                        R.id.recyclerViewPriceReport
                );

        txtReportSummary =
                findViewById(
                        R.id.txtReportSummary
                );

        txtEmptyPriceReport =
                findViewById(
                        R.id.txtEmptyPriceReport
                );

        progressPriceReport =
                findViewById(
                        R.id.progressPriceReport
                );

        recyclerViewPriceReport.setLayoutManager(
                new LinearLayoutManager(
                        PriceAnalysisActivity.this
                )
        );

        recyclerViewPriceReport.setHasFixedSize(
                true
        );

        adapter = new PriceAnalysisAdapter();

        recyclerViewPriceReport.setAdapter(
                adapter
        );
    }

    private void addEvents() {
        btnBackPriceReport.setOnClickListener(
                view -> finish()
        );

        btnRefreshPriceReport.setOnClickListener(
                view -> loadPriceAnalysis()
        );
    }

    private void loadPriceAnalysis() {
        if (isLoading) {
            return;
        }

        setLoadingState(true);

        executorService.execute(() -> {
            try {
                ArrayList<CategoryPriceReport> reports =
                        ReportDAO.getCategoryPriceAnalysis(
                                getApplicationContext()
                        );

                runOnUiThread(() -> {
                    adapter.setReports(reports);

                    txtReportSummary.setText(
                            "Tổng số danh mục phân tích: "
                                    + reports.size()
                    );

                    boolean isEmpty =
                            reports.isEmpty();

                    txtEmptyPriceReport.setVisibility(
                            isEmpty
                                    ? View.VISIBLE
                                    : View.GONE
                    );

                    recyclerViewPriceReport.setVisibility(
                            isEmpty
                                    ? View.GONE
                                    : View.VISIBLE
                    );

                    setLoadingState(false);
                });

            } catch (Exception exception) {
                runOnUiThread(() -> {
                    setLoadingState(false);

                    txtEmptyPriceReport.setVisibility(
                            View.VISIBLE
                    );

                    txtEmptyPriceReport.setText(
                            "Không thể tải báo cáo"
                    );

                    Toast.makeText(
                            PriceAnalysisActivity.this,
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

        progressPriceReport.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );

        btnRefreshPriceReport.setEnabled(
                !loading
        );
    }

    @Override
    protected void onDestroy() {
        executorService.shutdownNow();

        super.onDestroy();
    }
}