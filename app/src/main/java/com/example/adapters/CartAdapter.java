package com.example.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mcommercemobile.R;
import com.example.models.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter
        extends RecyclerView.Adapter<
        CartAdapter.CartViewHolder
        > {

    public interface CartActionListener {

        void onIncrease(CartItem cartItem);

        void onDecrease(CartItem cartItem);

        void onDelete(CartItem cartItem);
    }

    private final ArrayList<CartItem> cartItems =
            new ArrayList<>();

    private final CartActionListener listener;

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    new Locale("vi", "VN")
            );

    public CartAdapter(
            CartActionListener listener
    ) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_cart,
                                parent,
                                false
                        );

        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CartViewHolder holder,
            int position
    ) {
        CartItem item =
                cartItems.get(position);

        holder.txtProductName.setText(
                item.getProductName()
        );

        holder.txtUnitPrice.setText(
                currencyFormat.format(
                        item.getUnitPrice()
                )
        );

        holder.txtQuantity.setText(
                String.valueOf(
                        item.getQuantity()
                )
        );

        holder.txtSubtotal.setText(
                "Thành tiền: "
                        + currencyFormat.format(
                        item.getSubtotal()
                )
        );

        Glide.with(holder.itemView)
                .load(item.getImageURL())
                .centerCrop()
                .placeholder(
                        android.R.drawable
                                .ic_menu_gallery
                )
                .error(
                        android.R.drawable
                                .ic_menu_report_image
                )
                .into(holder.imgProduct);

        holder.btnIncrease.setOnClickListener(
                view -> listener.onIncrease(
                        item
                )
        );

        holder.btnDecrease.setOnClickListener(
                view -> listener.onDecrease(
                        item
                )
        );

        holder.btnDelete.setOnClickListener(
                view -> listener.onDelete(
                        item
                )
        );
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void setCartItems(
            List<CartItem> newItems
    ) {
        cartItems.clear();

        if (newItems != null) {
            cartItems.addAll(newItems);
        }

        notifyDataSetChanged();
    }

    public static class CartViewHolder
            extends RecyclerView.ViewHolder {

        private final ImageView imgProduct;

        private final TextView txtProductName;
        private final TextView txtUnitPrice;
        private final TextView txtQuantity;
        private final TextView txtSubtotal;

        private final Button btnIncrease;
        private final Button btnDecrease;

        private final ImageButton btnDelete;

        public CartViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            imgProduct =
                    itemView.findViewById(
                            R.id.imgCartProduct
                    );

            txtProductName =
                    itemView.findViewById(
                            R.id.txtCartProductName
                    );

            txtUnitPrice =
                    itemView.findViewById(
                            R.id.txtCartUnitPrice
                    );

            txtQuantity =
                    itemView.findViewById(
                            R.id.txtCartQuantity
                    );

            txtSubtotal =
                    itemView.findViewById(
                            R.id.txtCartSubtotal
                    );

            btnIncrease =
                    itemView.findViewById(
                            R.id.btnIncrease
                    );

            btnDecrease =
                    itemView.findViewById(
                            R.id.btnDecrease
                    );

            btnDelete =
                    itemView.findViewById(
                            R.id.btnDeleteCartItem
                    );
        }
    }
}