package com.example.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mcommercemobile.R;
import com.example.models.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter
        extends RecyclerView.Adapter<
        ProductAdapter.ProductViewHolder
        > {

    private final ArrayList<Product> products =
            new ArrayList<>();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    new Locale("vi", "VN")
            );

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_product,
                                parent,
                                false
                        );

        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProductViewHolder holder,
            int position
    ) {
        Product product =
                products.get(position);

        holder.txtProductName.setText(
                product.getProductName()
        );

        holder.txtRating.setText(
                String.format(
                        Locale.getDefault(),
                        "★ %.1f",
                        product.getRating()
                )
        );

        holder.txtPromotionalPrice.setText(
                currencyFormat.format(
                        product.getPromotionalPrice()
                )
        );

        if (
                product.getOriginalPrice()
                        > product.getPromotionalPrice()
        ) {
            holder.txtOriginalPrice.setVisibility(
                    View.VISIBLE
            );

            holder.txtOriginalPrice.setText(
                    currencyFormat.format(
                            product.getOriginalPrice()
                    )
            );

            holder.txtOriginalPrice.setPaintFlags(
                    holder.txtOriginalPrice
                            .getPaintFlags()
                            | Paint.STRIKE_THRU_TEXT_FLAG
            );

        } else {
            holder.txtOriginalPrice.setVisibility(
                    View.GONE
            );
        }

        String imageURL =
                product.getImageURL();

        Glide.with(holder.itemView)
                .load(imageURL)
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
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    /**
     * Thêm trang mới vào cuối danh sách hiện tại.
     */
    public void addProducts(
            List<Product> newProducts
    ) {
        if (newProducts == null
                || newProducts.isEmpty()) {
            return;
        }

        int startPosition =
                products.size();

        products.addAll(newProducts);

        notifyItemRangeInserted(
                startPosition,
                newProducts.size()
        );
    }

    /**
     * Xóa dữ liệu cũ trước khi chạy một truy vấn mới.
     *
     * Ví dụ:
     * - Người dùng tìm từ khóa mới.
     * - Người dùng thay đổi khoảng giá.
     * - Người dùng nhấn đặt lại.
     */
    public void clearProducts() {
        int oldSize = products.size();

        products.clear();

        if (oldSize > 0) {
            notifyItemRangeRemoved(
                    0,
                    oldSize
            );
        }
    }

    public static class ProductViewHolder
            extends RecyclerView.ViewHolder {

        private final ImageView imgProduct;

        private final TextView txtProductName;
        private final TextView txtRating;

        private final TextView txtPromotionalPrice;
        private final TextView txtOriginalPrice;

        public ProductViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            imgProduct =
                    itemView.findViewById(
                            R.id.imgProduct
                    );

            txtProductName =
                    itemView.findViewById(
                            R.id.txtProductName
                    );

            txtRating =
                    itemView.findViewById(
                            R.id.txtRating
                    );

            txtPromotionalPrice =
                    itemView.findViewById(
                            R.id.txtPromotionalPrice
                    );

            txtOriginalPrice =
                    itemView.findViewById(
                            R.id.txtOriginalPrice
                    );
        }
    }
}