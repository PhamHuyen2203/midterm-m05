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

import com.example.adapters.AdminOrderAdapter;
import com.example.dals.OrderDAO;
import com.example.models.AdminOrderItem;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminOrderActivity
        extends AppCompatActivity {

    private static final String TAG =
            "Q10AdminOrder";

    /**
     * Đề yêu cầu phân trang.
     * Mỗi trang tải 10 đơn hàng.
     */
    private static final int PAGE_SIZE = 10;

    private ImageButton btnBackAdminOrder;
    private ImageButton btnRefreshAdminOrder;

    private RecyclerView recyclerViewAdminOrder;

    private TextView txtAdminOrderSummary;
    private TextView txtEmptyAdminOrder;
    private TextView txtAdminOrderLoadState;

    private ProgressBar progressAdminOrder;

    private AdminOrderAdapter adapter;
    private LinearLayoutManager layoutManager;

    /**
     * OFFSET của trang tiếp theo.
     *
     * Ban đầu: 0
     * Trang hai: 10
     * Trang ba: 20
     */
    private int currentOffset = 0;

    /**
     * Ngăn tải nhiều truy vấn cùng lúc.
     */
    private boolean isLoading = false;

    /**
     * false khi đã tải hết dữ liệu.
     */
    private boolean hasMoreData = true;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_admin_order
        );

        addViews();
        addEvents();

        loadNextPage();
    }

    private void addViews() {
        btnBackAdminOrder = findViewById(
                R.id.btnBackAdminOrder
        );

        btnRefreshAdminOrder = findViewById(
                R.id.btnRefreshAdminOrder
        );

        recyclerViewAdminOrder = findViewById(
                R.id.recyclerViewAdminOrder
        );

        txtAdminOrderSummary = findViewById(
                R.id.txtAdminOrderSummary
        );

        txtEmptyAdminOrder = findViewById(
                R.id.txtEmptyAdminOrder
        );

        txtAdminOrderLoadState = findViewById(
                R.id.txtAdminOrderLoadState
        );

        progressAdminOrder = findViewById(
                R.id.progressAdminOrder
        );

        layoutManager = new LinearLayoutManager(
                AdminOrderActivity.this
        );

        recyclerViewAdminOrder.setLayoutManager(
                layoutManager
        );

        recyclerViewAdminOrder.setHasFixedSize(
                true
        );

        adapter = new AdminOrderAdapter();

        recyclerViewAdminOrder.setAdapter(
                adapter
        );
    }

    private void addEvents() {
        btnBackAdminOrder.setOnClickListener(
                view -> finish()
        );

        btnRefreshAdminOrder.setOnClickListener(
                view -> refreshOrders()
        );

        recyclerViewAdminOrder.addOnScrollListener(
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
                         * dy > 0 nghĩa là đang cuộn xuống.
                         */
                        if (dy <= 0) {
                            return;
                        }

                        int totalItemCount =
                                layoutManager.getItemCount();

                        if (totalItemCount == 0) {
                            return;
                        }

                        int lastVisiblePosition =
                                layoutManager
                                        .findLastVisibleItemPosition();

                        /*
                         * Khi còn cách cuối danh sách hai item,
                         * tải thêm trang tiếp theo.
                         */
                        boolean reachedBottom =
                                lastVisiblePosition
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
     * Xóa danh sách cũ và tải lại từ OFFSET 0.
     */
    private void refreshOrders() {
        if (isLoading) {
            return;
        }

        adapter.clearOrders();

        currentOffset = 0;
        hasMoreData = true;

        txtAdminOrderSummary.setText(
                "Đã tải: 0 đơn hàng"
        );

        txtEmptyAdminOrder.setVisibility(
                View.GONE
        );

        txtAdminOrderLoadState.setVisibility(
                View.GONE
        );

        recyclerViewAdminOrder.setVisibility(
                View.VISIBLE
        );

        recyclerViewAdminOrder.scrollToPosition(
                0
        );

        loadNextPage();
    }

    /**
     * Tải một trang đơn hàng.
     */
    private void loadNextPage() {
        if (isLoading || !hasMoreData) {
            return;
        }

        isLoading = true;

        progressAdminOrder.setVisibility(
                View.VISIBLE
        );

        txtAdminOrderLoadState.setVisibility(
                View.GONE
        );

        btnRefreshAdminOrder.setEnabled(
                false
        );

        final int requestedOffset =
                currentOffset;

        Log.d(
                TAG,
                "Load paid orders: LIMIT="
                        + PAGE_SIZE
                        + ", OFFSET="
                        + requestedOffset
        );

        executorService.execute(() -> {
            try {
                ArrayList<AdminOrderItem> newOrders =
                        OrderDAO.getPaidOrdersPage(
                                getApplicationContext(),
                                PAGE_SIZE,
                                requestedOffset
                        );

                runOnUiThread(() ->
                        displayOrders(
                                newOrders,
                                requestedOffset
                        )
                );

            } catch (Exception exception) {
                Log.e(
                        TAG,
                        "Lỗi tải đơn hàng",
                        exception
                );

                runOnUiThread(() ->
                        displayError(exception)
                );
            }
        });
    }

    /**
     * Thêm trang mới vào RecyclerView.
     */
    private void displayOrders(
            ArrayList<AdminOrderItem> newOrders,
            int requestedOffset
    ) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        adapter.addOrders(
                newOrders
        );

        /*
         * OFFSET tăng đúng bằng số dòng vừa nhận.
         */
        currentOffset += newOrders.size();

        isLoading = false;

        progressAdminOrder.setVisibility(
                View.GONE
        );

        btnRefreshAdminOrder.setEnabled(
                true
        );

        int loadedCount =
                adapter.getItemCount();

        txtAdminOrderSummary.setText(
                "Đã tải: "
                        + loadedCount
                        + " đơn hàng đã thanh toán"
        );

        Log.d(
                TAG,
                "Completed OFFSET="
                        + requestedOffset
                        + ", received="
                        + newOrders.size()
                        + ", total="
                        + loadedCount
        );

        boolean isEmpty =
                loadedCount == 0;

        txtEmptyAdminOrder.setVisibility(
                isEmpty
                        ? View.VISIBLE
                        : View.GONE
        );

        recyclerViewAdminOrder.setVisibility(
                isEmpty
                        ? View.GONE
                        : View.VISIBLE
        );

        /*
         * Nếu nhận dưới 10 dòng thì đây là trang cuối.
         */
        if (newOrders.size() < PAGE_SIZE) {
            hasMoreData = false;

            if (!isEmpty) {
                txtAdminOrderLoadState.setVisibility(
                        View.VISIBLE
                );

                txtAdminOrderLoadState.setText(
                        "Đã tải hết đơn hàng"
                );
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

        progressAdminOrder.setVisibility(
                View.GONE
        );

        btnRefreshAdminOrder.setEnabled(
                true
        );

        txtAdminOrderLoadState.setVisibility(
                View.VISIBLE
        );

        txtAdminOrderLoadState.setText(
                "Không thể tải đơn hàng"
        );

        Toast.makeText(
                AdminOrderActivity.this,
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