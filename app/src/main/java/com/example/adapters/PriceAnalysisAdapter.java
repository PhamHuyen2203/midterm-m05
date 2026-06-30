package com.example.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mcommercemobile.R;
import com.example.models.CategoryPriceReport;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PriceAnalysisAdapter
        extends RecyclerView.Adapter<
        PriceAnalysisAdapter.PriceViewHolder
        > {

    private final ArrayList<CategoryPriceReport> reports =
            new ArrayList<>();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    new Locale("vi", "VN")
            );

    @NonNull
    @Override
    public PriceViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_price_analysis,
                                parent,
                                false
                        );

        return new PriceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PriceViewHolder holder,
            int position
    ) {
        CategoryPriceReport report =
                reports.get(position);

        holder.txtCategoryName.setText(
                report.getCategoryName()
        );

        holder.txtAveragePrice.setText(
                currencyFormat.format(
                        report.getAveragePrice()
                )
        );

        holder.txtLowestPrice.setText(
                currencyFormat.format(
                        report.getLowestPrice()
                )
        );

        holder.txtHighestPrice.setText(
                currencyFormat.format(
                        report.getHighestPrice()
                )
        );
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public void setReports(
            List<CategoryPriceReport> newReports
    ) {
        reports.clear();

        if (newReports != null) {
            reports.addAll(newReports);
        }

        notifyDataSetChanged();
    }

    public static class PriceViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView txtCategoryName;
        private final TextView txtAveragePrice;
        private final TextView txtLowestPrice;
        private final TextView txtHighestPrice;

        public PriceViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            txtCategoryName =
                    itemView.findViewById(
                            R.id.txtReportCategoryName
                    );

            txtAveragePrice =
                    itemView.findViewById(
                            R.id.txtAveragePrice
                    );

            txtLowestPrice =
                    itemView.findViewById(
                            R.id.txtLowestPrice
                    );

            txtHighestPrice =
                    itemView.findViewById(
                            R.id.txtHighestPrice
                    );
        }
    }
}