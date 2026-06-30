package com.example.mcommercemobile;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductActivity
        extends AppCompatActivity {

    private static final String TAG =
            "Q4Q5Product";

    /**
     * Câu 4:
     * Mỗi lần chỉ tải tối đa 10 sản phẩm.
     */
    private static final int PAGE_SIZE = 10;

    private ImageButton btnBack;

    private EditText edtKeyword;
    private EditText edtMinPrice;
    private EditText edtMaxPrice;

    private Button btnSearch;
    private Button btnReset;

    private RecyclerView recyclerViewProduct;

    private TextView txtFilterSummary;
    private TextView txtLoadedCount;
    private TextView txtLoadState;

    private ProgressBar progressLoading;

    private ProductAdapter productAdapter;
    private LinearLayoutManager layoutManager;

    private int currentOffset = 0;

    private boolean isLoading = false;
    private boolean hasMoreData = true;

    /**
     * false:
     * Hiển thị toàn bộ sản phẩm của Câu 4.
     *
     * true:
     * Tìm kiếm và lọc của Câu 5.
     */
    private boolean isSearchMode = false;

    private String activeKeyword = "";
    private long activeMinPrice = 0;
    private long activeMaxPrice = 0;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private final NumberFormat priceFormat =
            NumberFormat.getNumberInstance(
                    new Locale("vi", "VN")
            );

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_product
        );

        addViews();
        addEvents();

        /*
         * Mở màn hình:
         * tải 10 sản phẩm đầu tiên của Câu 4.
         */
        loadNextPage();
    }

    private void addViews() {
        btnBack = findViewById(
                R.id.btnBack
        );

        edtKeyword = findViewById(
                R.id.edtKeyword
        );

        edtMinPrice = findViewById(
                R.id.edtMinPrice
        );

        edtMaxPrice = findViewById(
                R.id.edtMaxPrice
        );

        btnSearch = findViewById(
                R.id.btnSearch
        );

        btnReset = findViewById(
                R.id.btnReset
        );

        recyclerViewProduct = findViewById(
                R.id.recyclerViewProduct
        );

        txtFilterSummary = findViewById(
                R.id.txtFilterSummary
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

        recyclerViewProduct.setHasFixedSize(
                true
        );

        productAdapter = new ProductAdapter();

        recyclerViewProduct.setAdapter(
                productAdapter
        );
    }

    private void addEvents() {
        btnBack.setOnClickListener(
                view -> finish()
        );

        btnSearch.setOnClickListener(
                view -> performSearch()
        );

        btnReset.setOnClickListener(
                view -> resetSearch()
        );

        /*
         * Nhấn nút Search trên bàn phím tại ô từ khóa.
         */
        edtKeyword.setOnEditorActionListener(
                (textView, actionId, event) -> {
                    if (actionId
                            == EditorInfo.IME_ACTION_SEARCH) {

                        performSearch();
                        return true;
                    }

                    return false;
                }
        );

        /*
         * Nhấn Search tại ô giá cao nhất.
         */
        edtMaxPrice.setOnEditorActionListener(
                (textView, actionId, event) -> {
                    if (actionId
                            == EditorInfo.IME_ACTION_SEARCH) {

                        performSearch();
                        return true;
                    }

                    return false;
                }
        );

        /*
         * Giữ nguyên Infinite Scroll của Câu 4.
         * Khi đang tìm kiếm, nó sẽ tải tiếp kết quả
         * đã được lọc.
         */
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

                        if (dy <= 0) {
                            return;
                        }

                        int totalItemCount =
                                layoutManager
                                        .getItemCount();

                        if (totalItemCount == 0) {
                            return;
                        }

                        int lastVisibleItem =
                                layoutManager
                                        .findLastVisibleItemPosition();

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
     * Đọc và kiểm tra dữ liệu người dùng nhập.
     */
    private void performSearch() {
        if (isLoading) {
            return;
        }

        String keyword =
                edtKeyword.getText()
                        .toString()
                        .trim();

        String minPriceText =
                edtMinPrice.getText()
                        .toString()
                        .trim();

        String maxPriceText =
                edtMaxPrice.getText()
                        .toString()
                        .trim();

        if (keyword.isEmpty()) {
            edtKeyword.setError(
                    "Vui lòng nhập tên sản phẩm."
            );

            edtKeyword.requestFocus();
            return;
        }

        if (minPriceText.isEmpty()) {
            edtMinPrice.setError(
                    "Vui lòng nhập giá thấp nhất."
            );

            edtMinPrice.requestFocus();
            return;
        }

        if (maxPriceText.isEmpty()) {
            edtMaxPrice.setError(
                    "Vui lòng nhập giá cao nhất."
            );

            edtMaxPrice.requestFocus();
            return;
        }

        long minPrice;
        long maxPrice;

        try {
            minPrice = parsePrice(
                    minPriceText
            );

            maxPrice = parsePrice(
                    maxPriceText
            );

        } catch (NumberFormatException exception) {
            Toast.makeText(
                    ProductActivity.this,
                    "Giá nhập vào không hợp lệ.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (minPrice > maxPrice) {
            edtMaxPrice.setError(
                    "Giá cao nhất phải lớn hơn hoặc bằng giá thấp nhất."
            );

            edtMaxPrice.requestFocus();
            return;
        }

        activeKeyword = keyword;
        activeMinPrice = minPrice;
        activeMaxPrice = maxPrice;

        isSearchMode = true;

        txtFilterSummary.setVisibility(
                View.VISIBLE
        );

        txtFilterSummary.setText(
                "Từ khóa: "
                        + activeKeyword
                        + " | Giá: "
                        + priceFormat.format(
                        activeMinPrice
                )
                        + " - "
                        + priceFormat.format(
                        activeMaxPrice
                )
                        + " VNĐ"
        );

        hideKeyboard();

        resetPaginationAndLoad();
    }

    /**
     * Xóa điều kiện tìm kiếm và quay về danh sách
     * sản phẩm ban đầu của Câu 4.
     */
    private void resetSearch() {
        if (isLoading) {
            return;
        }

        edtKeyword.setText("");
        edtMinPrice.setText("");
        edtMaxPrice.setText("");

        edtKeyword.setError(null);
        edtMinPrice.setError(null);
        edtMaxPrice.setError(null);

        activeKeyword = "";
        activeMinPrice = 0;
        activeMaxPrice = 0;

        isSearchMode = false;

        txtFilterSummary.setVisibility(
                View.GONE
        );

        hideKeyboard();

        resetPaginationAndLoad();
    }

    /**
     * Bắt đầu một truy vấn mới từ OFFSET 0.
     */
    private void resetPaginationAndLoad() {
        productAdapter.clearProducts();

        currentOffset = 0;
        hasMoreData = true;

        txtLoadedCount.setText(
                "Đã tải: 0 sản phẩm"
        );

        txtLoadState.setVisibility(
                View.GONE
        );

        recyclerViewProduct.scrollToPosition(
                0
        );

        loadNextPage();
    }

    /**
     * Tải trang tiếp theo.
     *
     * Chế độ thường:
     * ProductDAO.getProductsPage()
     *
     * Chế độ tìm kiếm:
     * ProductDAO.searchProductsPage()
     */
    private void loadNextPage() {
        if (isLoading || !hasMoreData) {
            return;
        }

        isLoading = true;

        setLoadingState(true);

        final int requestedOffset =
                currentOffset;

        final boolean requestedSearchMode =
                isSearchMode;

        final String requestedKeyword =
                activeKeyword;

        final long requestedMinPrice =
                activeMinPrice;

        final long requestedMaxPrice =
                activeMaxPrice;

        Log.d(
                TAG,
                "Load: searchMode="
                        + requestedSearchMode
                        + ", keyword="
                        + requestedKeyword
                        + ", min="
                        + requestedMinPrice
                        + ", max="
                        + requestedMaxPrice
                        + ", LIMIT="
                        + PAGE_SIZE
                        + ", OFFSET="
                        + requestedOffset
        );

        executorService.execute(() -> {
            try {
                ArrayList<Product> newProducts;

                if (requestedSearchMode) {
                    newProducts =
                            ProductDAO.searchProductsPage(
                                    getApplicationContext(),
                                    requestedKeyword,
                                    requestedMinPrice,
                                    requestedMaxPrice,
                                    PAGE_SIZE,
                                    requestedOffset
                            );

                } else {
                    newProducts =
                            ProductDAO.getProductsPage(
                                    getApplicationContext(),
                                    PAGE_SIZE,
                                    requestedOffset
                            );
                }

                runOnUiThread(() ->
                        displayProducts(
                                newProducts,
                                requestedOffset,
                                requestedSearchMode
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

    private void displayProducts(
            ArrayList<Product> newProducts,
            int requestedOffset,
            boolean requestedSearchMode
    ) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        productAdapter.addProducts(
                newProducts
        );

        currentOffset +=
                newProducts.size();

        isLoading = false;

        setLoadingState(false);

        int loadedCount =
                productAdapter.getItemCount();

        if (requestedSearchMode) {
            txtLoadedCount.setText(
                    "Đã tải: "
                            + loadedCount
                            + " kết quả phù hợp"
            );

        } else {
            txtLoadedCount.setText(
                    "Đã tải: "
                            + loadedCount
                            + " sản phẩm"
            );
        }

        Log.d(
                TAG,
                "Hoàn thành OFFSET="
                        + requestedOffset
                        + ": nhận "
                        + newProducts.size()
                        + ", tổng="
                        + loadedCount
        );

        if (newProducts.size() < PAGE_SIZE) {
            hasMoreData = false;

            txtLoadState.setVisibility(
                    View.VISIBLE
            );

            if (loadedCount == 0) {
                if (requestedSearchMode) {
                    txtLoadState.setText(
                            "Không tìm thấy sản phẩm phù hợp"
                    );

                } else {
                    txtLoadState.setText(
                            "Không có sản phẩm"
                    );
                }

            } else {
                if (requestedSearchMode) {
                    txtLoadState.setText(
                            "Đã tải hết kết quả tìm kiếm"
                    );

                } else {
                    txtLoadState.setText(
                            "Đã tải hết sản phẩm"
                    );
                }
            }
        }
    }

    private void displayError(
            Exception exception
    ) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        isLoading = false;
        hasMoreData = false;

        setLoadingState(false);

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

    private void setLoadingState(
            boolean loading
    ) {
        progressLoading.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );

        btnSearch.setEnabled(
                !loading
        );

        btnReset.setEnabled(
                !loading
        );

        if (loading) {
            txtLoadState.setVisibility(
                    View.GONE
            );
        }
    }

    /**
     * Hỗ trợ cả trường hợp người dùng nhập:
     *
     * 100000
     * 100.000
     * 100,000
     */
    private long parsePrice(
            String priceText
    ) throws NumberFormatException {

        String normalizedPrice =
                priceText.replaceAll(
                        "[^0-9]",
                        ""
                );

        if (normalizedPrice.isEmpty()) {
            throw new NumberFormatException(
                    "Giá rỗng."
            );
        }

        return Long.parseLong(
                normalizedPrice
        );
    }

    private void hideKeyboard() {
        View currentView =
                getCurrentFocus();

        if (currentView == null) {
            return;
        }

        InputMethodManager inputMethodManager =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    currentView.getWindowToken(),
                    0
            );
        }

        currentView.clearFocus();
    }

    @Override
    protected void onDestroy() {
        executorService.shutdownNow();

        super.onDestroy();
    }
}