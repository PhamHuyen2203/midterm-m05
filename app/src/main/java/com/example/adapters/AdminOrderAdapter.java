package com.example.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mcommercemobile.R;
import com.example.models.AdminOrderItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter
        extends RecyclerView.Adapter<
        AdminOrderAdapter.AdminOrderViewHolder
        > {

    private final ArrayList<AdminOrderItem> orders =
            new ArrayList<>();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    new Locale("vi", "VN")
            );

    @NonNull
    @Override
    public AdminOrderViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_admin_order,
                                parent,
                                false
                        );

        return new AdminOrderViewHolder(
                view
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull AdminOrderViewHolder holder,
            int position
    ) {
        AdminOrderItem order =
                orders.get(position);

        holder.txtOrderID.setText(
                "Đơn hàng #" + order.getOrderID()
        );

        String paymentStatus =
                order.getPaymentStatus();

        if (paymentStatus == null) {
            paymentStatus = "";
        }

        holder.txtPaymentStatus.setText(
                paymentStatus.toUpperCase(
                        Locale.getDefault()
                )
        );

        holder.txtCustomerName.setText(
                "Khách hàng: "
                        + order.getCustomerName()
        );

        holder.txtCustomerEmail.setText(
                order.getCustomerEmail()
        );

        holder.txtOrderDate.setText(
                "Ngày đặt: "
                        + order.getOrderDate()
        );

        holder.txtShippingAddress.setText(
                "Giao đến: "
                        + order.getShippingAddress()
        );

        holder.txtPaymentMethod.setText(
                "Thanh toán: "
                        + order.getPaymentMethod()
        );

        holder.txtOrderTotal.setText(
                "Tổng tiền: "
                        + currencyFormat.format(
                        order.getTotalAmount()
                )
        );
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    /**
     * Thêm một trang đơn hàng mới vào cuối danh sách.
     */
    public void addOrders(
            List<AdminOrderItem> newOrders
    ) {
        if (newOrders == null
                || newOrders.isEmpty()) {
            return;
        }

        int startPosition =
                orders.size();

        orders.addAll(newOrders);

        notifyItemRangeInserted(
                startPosition,
                newOrders.size()
        );
    }

    /**
     * Xóa danh sách khi quản trị viên nhấn làm mới.
     */
    public void clearOrders() {
        int oldSize =
                orders.size();

        orders.clear();

        if (oldSize > 0) {
            notifyItemRangeRemoved(
                    0,
                    oldSize
            );
        }
    }

    public static class AdminOrderViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView txtOrderID;
        private final TextView txtPaymentStatus;

        private final TextView txtCustomerName;
        private final TextView txtCustomerEmail;

        private final TextView txtOrderDate;
        private final TextView txtShippingAddress;
        private final TextView txtPaymentMethod;

        private final TextView txtOrderTotal;

        public AdminOrderViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            txtOrderID = itemView.findViewById(
                    R.id.txtAdminOrderID
            );

            txtPaymentStatus = itemView.findViewById(
                    R.id.txtAdminPaymentStatus
            );

            txtCustomerName = itemView.findViewById(
                    R.id.txtAdminCustomerName
            );

            txtCustomerEmail = itemView.findViewById(
                    R.id.txtAdminCustomerEmail
            );

            txtOrderDate = itemView.findViewById(
                    R.id.txtAdminOrderDate
            );

            txtShippingAddress = itemView.findViewById(
                    R.id.txtAdminShippingAddress
            );

            txtPaymentMethod = itemView.findViewById(
                    R.id.txtAdminPaymentMethod
            );

            txtOrderTotal = itemView.findViewById(
                    R.id.txtAdminOrderTotal
            );
        }
    }
}