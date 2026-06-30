package com.example.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mcommercemobile.R;
import com.example.models.PotentialProductReport;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PotentialProductAdapter
        extends RecyclerView.Adapter<
        PotentialProductAdapter
                .PotentialProductViewHolder
        > {

    private final ArrayList<PotentialProductReport> reports =
            new ArrayList<>();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    new Locale("vi", "VN")
            );

    @NonNull
    @Override
    public PotentialProductViewHolder
    onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_potential_product,
                                parent,
                                false
                        );

        return new PotentialProductViewHolder(
                view
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull PotentialProductViewHolder holder,
            int position
    ) {
        PotentialProductReport report =
                reports.get(position);

        holder.txtRank.setText(
                "#" + (position + 1)
        );

        holder.txtProductName.setText(
                report.getProductName()
        );

        holder.txtRating.setText(
                String.format(
                        Locale.getDefault(),
                        "★ %.1f",
                        report.getRating()
                )
        );

        holder.txtOldPrice.setText(
                currencyFormat.format(
                        report.getOldPrice()
                )
        );

        holder.txtOldPrice.setPaintFlags(
                holder.txtOldPrice.getPaintFlags()
                        | Paint.STRIKE_THRU_TEXT_FLAG
        );

        holder.txtNewPrice.setText(
                currencyFormat.format(
                        report.getNewPrice()
                )
        );

        holder.txtAmountSaved.setText(
                currencyFormat.format(
                        report.getAmountSaved()
                )
        );
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public void setReports(
            List<PotentialProductReport> newReports
    ) {
        reports.clear();

        if (newReports != null) {
            reports.addAll(newReports);
        }

        notifyDataSetChanged();
    }

    public static class PotentialProductViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView txtRank;
        private final TextView txtProductName;
        private final TextView txtRating;
        private final TextView txtOldPrice;
        private final TextView txtNewPrice;
        private final TextView txtAmountSaved;

        public PotentialProductViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            txtRank = itemView.findViewById(
                    R.id.txtPotentialRank
            );

            txtProductName = itemView.findViewById(
                    R.id.txtPotentialProductName
            );

            txtRating = itemView.findViewById(
                    R.id.txtPotentialRating
            );

            txtOldPrice = itemView.findViewById(
                    R.id.txtPotentialOldPrice
            );

            txtNewPrice = itemView.findViewById(
                    R.id.txtPotentialNewPrice
            );

            txtAmountSaved = itemView.findViewById(
                    R.id.txtPotentialAmountSaved
            );
        }
    }
}