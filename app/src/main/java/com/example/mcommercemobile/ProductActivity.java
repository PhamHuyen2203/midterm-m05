package com.example.mcommercemobile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapters.ProductAdapter;
import com.example.dals.ProductDAO;
import com.example.models.Product;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductActivity extends AppCompatActivity {

    private static final String TAG =
            "Q4Pagination";

    /**
     * Theo đề bài, mỗi lần cuộn chỉ tải thêm
     * 10 sản phẩm tiếp theo.
     */
    private static final int PAGE_SIZE = 10;

    private ImageButton btnBack;

    private RecyclerView recyclerViewProduct;

    private TextView txtLoadedCount;
    private TextView txtLoadState;

    private ProgressBar progressLoading;

    private ProductAdapter productAdapter;

    private LinearLayoutManager layoutManager;

    /**
     * OFFSET cho lần truy vấn tiếp theo.
     *
     * Ban đầu = 0.
     * Sau trang đầu = 10.
     * Sau trang hai = 20.
     */
    private int currentOffset = 0;

    /**
     * Ngăn chương trình gọi nhiều truy vấn
     * cùng một lúc khi người dùng cuộn nhanh.
     */
    private boolean isLoading = false;

    /**
     * false khi database không còn sản phẩm.
     */
    private boolean hasMoreData = true;

    /**
     * Thực hiện truy vấn SQLite ở luồng nền,
     * tránh làm treo giao diện.
     */
    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_product
        );

        addViews();
        addEvents();

        /*
         * Khi mở màn hình, chỉ tải trang đầu,
         * tương đương LIMIT 10 OFFSET 0.
         */
        loadNextPage();
    }

    private void addViews() {
        btnBack = findViewById(
                R.id.btnBack
        );

        recyclerViewProduct = findViewById(
                R.id.recyclerViewProduct
        );

        txtLoadedCount = findViewById(
                R.id.txtLoadedCount
        );

        txtLoadState = findViewById(
                R.id.txtLoadState
        );

        progressLoading = findViewById(
                R.id.progressLoading
        );

        layoutManager = new LinearLayoutManager(
                ProductActivity.this
        );

        recyclerViewProduct.setLayoutManager(
                layoutManager
        );

        productAdapter = new ProductAdapter();

        recyclerViewProduct.setAdapter(
                productAdapter
        );
    }

    private void addEvents() {
        btnBack.setOnClickListener(view ->
                finish()
        );

        recyclerViewProduct.addOnScrollListener(
                new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrolled(
                            RecyclerView recyclerView,
                            int dx,
                            int dy
                    ) {
                        super.onScrolled(
                                recyclerView,
                                dx,
                                dy
                        );

                        /*
                         * dy > 0 nghĩa là người dùng
                         * đang cuộn xuống.
                         */
                        if (dy <= 0) {
                            return;
                        }

                        int totalItemCount =
                                layoutManager.getItemCount();

                        int lastVisibleItem =
                                layoutManager
                                        .findLastVisibleItemPosition();

                        /*
                         * Khi người dùng chỉ còn cách cuối
                         * danh sách hai sản phẩm, tải trang mới.
                         */
                        boolean reachedBottom =
                                lastVisibleItem
                                        >= totalItemCount - 2;

                        if (
                                reachedBottom
                                        && !isLoading
                                        && hasMoreData
                        ) {
                            loadNextPage();
                        }
                    }
                }
        );
    }

    /**
     * Tải một trang gồm tối đa 10 sản phẩm.
     */
    private void loadNextPage() {
        if (isLoading || !hasMoreData) {
            return;
        }

        isLoading = true;

        progressLoading.setVisibility(
                View.VISIBLE
        );

        txtLoadState.setVisibility(
                View.GONE
        );

        /*
         * Lưu OFFSET được yêu cầu để dùng
         * trong log và kiểm tra kết quả.
         */
        int requestedOffset = currentOffset;

        Log.d(
                TAG,
                "Bắt đầu truy vấn: LIMIT="
                        + PAGE_SIZE
                        + ", OFFSET="
                        + requestedOffset
        );

        executorService.execute(() -> {
            try {
                ArrayList<Product> newProducts =
                        ProductDAO.getProductsPage(
                                getApplicationContext(),
                                PAGE_SIZE,
                                requestedOffset
                        );

                runOnUiThread(() ->
                        displayProducts(
                                newProducts,
                                requestedOffset
                        )
                );

            } catch (Exception exception) {
                Log.e(
                        TAG,
                        "Lỗi tải sản phẩm",
                        exception
                );

                runOnUiThread(() ->
                        displayError(exception)
                );
            }
        });
    }

    /**
     * Thêm trang mới vào cuối RecyclerView.
     */
    private void displayProducts(
            ArrayList<Product> newProducts,
            int requestedOffset
    ) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        productAdapter.addProducts(
                newProducts
        );

        /*
         * OFFSET tăng đúng bằng số sản phẩm
         * vừa tải được.
         */
        currentOffset += newProducts.size();

        isLoading = false;

        progressLoading.setVisibility(
                View.GONE
        );

        txtLoadedCount.setText(
                "Đã tải: "
                        + productAdapter.getItemCount()
                        + " sản phẩm"
        );

        Log.d(
                TAG,
                "Hoàn thành OFFSET="
                        + requestedOffset
                        + ": nhận "
                        + newProducts.size()
                        + " sản phẩm, tổng="
                        + productAdapter.getItemCount()
        );

        /*
         * Nếu kết quả ít hơn 10 sản phẩm,
         * đây là trang cuối.
         */
        if (newProducts.size() < PAGE_SIZE) {
            hasMoreData = false;

            txtLoadState.setVisibility(
                    View.VISIBLE
            );

            if (productAdapter.getItemCount() == 0) {
                txtLoadState.setText(
                        "Không có sản phẩm"
                );

            } else {
                txtLoadState.setText(
                        "Đã tải hết sản phẩm"
                );
            }
        }
    }

    private void displayError(Exception exception) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        isLoading = false;

        progressLoading.setVisibility(
                View.GONE
        );

        txtLoadState.setVisibility(
                View.VISIBLE
        );

        txtLoadState.setText(
                "Không thể tải sản phẩm"
        );

        Toast.makeText(
                ProductActivity.this,
                "Lỗi: " + exception.getMessage(),
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    protected void onDestroy() {
        executorService.shutdownNow();

        super.onDestroy();
    }
}