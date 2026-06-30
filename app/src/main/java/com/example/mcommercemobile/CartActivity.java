package com.example.mcommercemobile;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapters.CartAdapter;
import com.example.dals.CartDAO;
import com.example.dals.OrderDAO;
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

    private EditText edtShippingAddress;

    private Spinner spinnerPaymentMethod;

    private Button btnCheckout;

    private ProgressBar progressCart;

    private CartAdapter cartAdapter;

    private int currentUserID = -1;

    private long currentTotalAmount = 0;

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
        setupPaymentMethods();
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

        edtShippingAddress = findViewById(
                R.id.edtShippingAddress
        );

        spinnerPaymentMethod = findViewById(
                R.id.spinnerPaymentMethod
        );

        btnCheckout = findViewById(
                R.id.btnCheckout
        );

        progressCart = findViewById(
                R.id.progressCart
        );

        recyclerViewCart.setLayoutManager(
                new LinearLayoutManager(
                        CartActivity.this
                )
        );

        recyclerViewCart.setHasFixedSize(
                true
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

    private void setupPaymentMethods() {
        String[] paymentMethods = {
                "Chuyển khoản ngân hàng",
                "Ví điện tử",
                "Thanh toán khi nhận hàng (COD)"
        };

        ArrayAdapter<String> paymentAdapter =
                new ArrayAdapter<>(
                        CartActivity.this,
                        android.R.layout
                                .simple_spinner_item,
                        paymentMethods
                );

        paymentAdapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item
        );

        spinnerPaymentMethod.setAdapter(
                paymentAdapter
        );
    }

    private void addEvents() {
        btnBackCart.setOnClickListener(
                view -> finish()
        );

        btnCheckout.setOnClickListener(
                view -> confirmCheckout()
        );
    }

    private void loadCart() {
        if (isWorking) {
            return;
        }

        setWorkingState(true);

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
                    setWorkingState(false);
                });

            } catch (Exception exception) {
                runOnUiThread(() -> {
                    setWorkingState(false);

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

        setWorkingState(true);

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
                    setWorkingState(false);

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
                    setWorkingState(false);

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

    /**
     * Kiểm tra thông tin rồi hiện hộp thoại xác nhận.
     */
    private void confirmCheckout() {
        if (isWorking) {
            return;
        }

        if (cartAdapter.getItemCount() == 0) {
            Toast.makeText(
                    CartActivity.this,
                    "Giỏ hàng đang trống.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String shippingAddress =
                edtShippingAddress.getText()
                        .toString()
                        .trim();

        if (shippingAddress.isEmpty()) {
            edtShippingAddress.setError(
                    "Vui lòng nhập địa chỉ giao hàng."
            );

            edtShippingAddress.requestFocus();
            return;
        }

        Object selectedItem =
                spinnerPaymentMethod
                        .getSelectedItem();

        if (selectedItem == null) {
            Toast.makeText(
                    CartActivity.this,
                    "Vui lòng chọn phương thức thanh toán.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String paymentMethod =
                selectedItem.toString();

        new AlertDialog.Builder(
                CartActivity.this
        )
                .setTitle(
                        "Xác nhận thanh toán"
                )
                .setMessage(
                        "Tổng tiền: "
                                + currencyFormat.format(
                                currentTotalAmount
                        )
                                + "\n\nĐịa chỉ: "
                                + shippingAddress
                                + "\n\nPhương thức: "
                                + paymentMethod
                )
                .setNegativeButton(
                        "Hủy",
                        null
                )
                .setPositiveButton(
                        "Thanh toán",
                        (dialog, which) ->
                                performCheckout(
                                        shippingAddress,
                                        paymentMethod
                                )
                )
                .show();
    }

    /**
     * Gọi OrderDAO để thực hiện transaction Checkout.
     */
    private void performCheckout(
            String shippingAddress,
            String paymentMethod
    ) {
        if (isWorking) {
            return;
        }

        setWorkingState(true);

        executorService.execute(() -> {
            try {
                if (currentUserID <= 0) {
                    currentUserID =
                            UserDAO.getOrCreateDemoCustomer(
                                    getApplicationContext()
                            );
                }

                OrderDAO.CheckoutResult result =
                        OrderDAO.checkout(
                                getApplicationContext(),
                                currentUserID,
                                shippingAddress,
                                paymentMethod
                        );

                /*
                 * Sau Checkout, Cart phải trống.
                 */
                ArrayList<CartItem> items =
                        CartDAO.getCartItems(
                                getApplicationContext(),
                                currentUserID
                        );

                runOnUiThread(() -> {
                    displayCart(items);

                    edtShippingAddress.setText("");

                    setWorkingState(false);

                    new AlertDialog.Builder(
                            CartActivity.this
                    )
                            .setTitle(
                                    "Thanh toán thành công"
                            )
                            .setMessage(
                                    "Mã đơn hàng: #"
                                            + result.getOrderID()
                                            + "\nSố lượng: "
                                            + result.getTotalQuantity()
                                            + "\nTổng tiền: "
                                            + currencyFormat.format(
                                            result
                                                    .getTotalAmount()
                                    )
                            )
                            .setPositiveButton(
                                    "Đóng",
                                    null
                            )
                            .show();
                });

            } catch (Exception exception) {
                runOnUiThread(() -> {
                    setWorkingState(false);

                    Toast.makeText(
                            CartActivity.this,
                            "Thanh toán thất bại: "
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

        currentTotalAmount = totalAmount;

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

        btnCheckout.setEnabled(
                !isWorking && !isEmpty
        );
    }

    private void setWorkingState(
            boolean working
    ) {
        isWorking = working;

        progressCart.setVisibility(
                working
                        ? View.VISIBLE
                        : View.GONE
        );

        btnCheckout.setEnabled(
                !working
                        && cartAdapter != null
                        && cartAdapter.getItemCount() > 0
        );

        edtShippingAddress.setEnabled(
                !working
        );

        spinnerPaymentMethod.setEnabled(
                !working
        );
    }

    @Override
    protected void onDestroy() {
        executorService.shutdownNow();

        super.onDestroy();
    }
}