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

import com.example.adapters.CartAdapter;
import com.example.dals.CartDAO;
import com.example.dals.UserDAO;
import com.example.models.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartActivity
        extends AppCompatActivity {

    private ImageButton btnBackCart;

    private RecyclerView recyclerViewCart;

    private TextView txtCartSummary;
    private TextView txtEmptyCart;
    private TextView txtCartTotal;

    private ProgressBar progressCart;

    private CartAdapter cartAdapter;

    private int currentUserID = -1;

    private boolean isWorking = false;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    new Locale("vi", "VN")
            );

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_cart
        );

        addViews();
        addEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadCart();
    }

    private void addViews() {
        btnBackCart = findViewById(
                R.id.btnBackCart
        );

        recyclerViewCart = findViewById(
                R.id.recyclerViewCart
        );

        txtCartSummary = findViewById(
                R.id.txtCartSummary
        );

        txtEmptyCart = findViewById(
                R.id.txtEmptyCart
        );

        txtCartTotal = findViewById(
                R.id.txtCartTotal
        );

        progressCart = findViewById(
                R.id.progressCart
        );

        recyclerViewCart.setLayoutManager(
                new LinearLayoutManager(
                        CartActivity.this
                )
        );

        cartAdapter = new CartAdapter(
                new CartAdapter.CartActionListener() {

                    @Override
                    public void onIncrease(
                            CartItem cartItem
                    ) {
                        increaseQuantity(
                                cartItem
                        );
                    }

                    @Override
                    public void onDecrease(
                            CartItem cartItem
                    ) {
                        decreaseQuantity(
                                cartItem
                        );
                    }

                    @Override
                    public void onDelete(
                            CartItem cartItem
                    ) {
                        deleteCartItem(
                                cartItem
                        );
                    }
                }
        );

        recyclerViewCart.setAdapter(
                cartAdapter
        );
    }

    private void addEvents() {
        btnBackCart.setOnClickListener(
                view -> finish()
        );
    }

    private void loadCart() {
        if (isWorking) {
            return;
        }

        isWorking = true;

        progressCart.setVisibility(
                View.VISIBLE
        );

        executorService.execute(() -> {
            try {
                currentUserID =
                        UserDAO.getOrCreateDemoCustomer(
                                getApplicationContext()
                        );

                ArrayList<CartItem> items =
                        CartDAO.getCartItems(
                                getApplicationContext(),
                                currentUserID
                        );

                runOnUiThread(() -> {
                    displayCart(items);

                    isWorking = false;

                    progressCart.setVisibility(
                            View.GONE
                    );
                });

            } catch (Exception exception) {
                runOnUiThread(() -> {
                    isWorking = false;

                    progressCart.setVisibility(
                            View.GONE
                    );

                    Toast.makeText(
                            CartActivity.this,
                            "Không thể tải giỏ hàng: "
                                    + exception.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void increaseQuantity(
            CartItem item
    ) {
        runCartOperation(
                userID -> CartDAO.increaseQuantity(
                        getApplicationContext(),
                        userID,
                        item.getCartID()
                ),
                "Đã tăng số lượng.",
                "Không thể tăng số lượng."
        );
    }

    private void decreaseQuantity(
            CartItem item
    ) {
        runCartOperation(
                userID -> CartDAO.decreaseQuantity(
                        getApplicationContext(),
                        userID,
                        item.getCartID()
                ),
                "Đã giảm số lượng.",
                "Số lượng tối thiểu là 1. "
                        + "Hãy dùng nút xóa để bỏ sản phẩm."
        );
    }

    private void deleteCartItem(
            CartItem item
    ) {
        runCartOperation(
                userID -> CartDAO.deleteCartItem(
                        getApplicationContext(),
                        userID,
                        item.getCartID()
                ),
                "Đã xóa sản phẩm khỏi giỏ.",
                "Không thể xóa sản phẩm."
        );
    }

    private interface CartOperation {

        boolean execute(int userID)
                throws Exception;
    }

    private void runCartOperation(
            CartOperation operation,
            String successMessage,
            String unchangedMessage
    ) {
        if (isWorking) {
            return;
        }

        isWorking = true;

        progressCart.setVisibility(
                View.VISIBLE
        );

        executorService.execute(() -> {
            try {
                if (currentUserID <= 0) {
                    currentUserID =
                            UserDAO.getOrCreateDemoCustomer(
                                    getApplicationContext()
                            );
                }

                boolean changed =
                        operation.execute(
                                currentUserID
                        );

                ArrayList<CartItem> items =
                        CartDAO.getCartItems(
                                getApplicationContext(),
                                currentUserID
                        );

                runOnUiThread(() -> {
                    displayCart(items);

                    isWorking = false;

                    progressCart.setVisibility(
                            View.GONE
                    );

                    Toast.makeText(
                            CartActivity.this,
                            changed
                                    ? successMessage
                                    : unchangedMessage,
                            Toast.LENGTH_SHORT
                    ).show();
                });

            } catch (Exception exception) {
                runOnUiThread(() -> {
                    isWorking = false;

                    progressCart.setVisibility(
                            View.GONE
                    );

                    Toast.makeText(
                            CartActivity.this,
                            "Lỗi giỏ hàng: "
                                    + exception.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void displayCart(
            ArrayList<CartItem> items
    ) {
        cartAdapter.setCartItems(
                items
        );

        int totalQuantity = 0;
        long totalAmount = 0;

        for (CartItem item : items) {
            totalQuantity +=
                    item.getQuantity();

            totalAmount +=
                    item.getSubtotal();
        }

        txtCartSummary.setText(
                items.size()
                        + " loại sản phẩm | "
                        + totalQuantity
                        + " sản phẩm"
        );

        txtCartTotal.setText(
                "Tổng cộng: "
                        + currencyFormat.format(
                        totalAmount
                )
        );

        boolean isEmpty =
                items.isEmpty();

        txtEmptyCart.setVisibility(
                isEmpty
                        ? View.VISIBLE
                        : View.GONE
        );

        recyclerViewCart.setVisibility(
                isEmpty
                        ? View.GONE
                        : View.VISIBLE
        );
    }

    @Override
    protected void onDestroy() {
        executorService.shutdownNow();

        super.onDestroy();
    }
}