package com.example.dals;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.models.CategoryPriceReport;
import com.example.models.PotentialProductReport;

import java.io.IOException;
import java.util.ArrayList;

public final class ReportDAO {

    private static final String TAG =
            "ReportDAO";

    private ReportDAO() {
    }

    /**
     * Câu 8:
     * Phân tích giá sản phẩm theo từng danh mục.
     *
     * Kết quả gồm:
     * - Tên danh mục.
     * - Giá trung bình.
     * - Giá thấp nhất.
     * - Giá cao nhất.
     */
    public static ArrayList<CategoryPriceReport>
    getCategoryPriceAnalysis(
            Context context
    ) throws IOException {

        ArrayList<CategoryPriceReport> reports =
                new ArrayList<>();

        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database =
                    DatabaseHelper.openDatabase(
                            context
                    );

            String sql =
                    "SELECT " +
                            "c.CategoryID, " +
                            "c.CategoryName, " +
                            "AVG(p.PromotionalPrice) " +
                            "AS AveragePrice, " +
                            "MIN(p.PromotionalPrice) " +
                            "AS LowestPrice, " +
                            "MAX(p.PromotionalPrice) " +
                            "AS HighestPrice " +
                            "FROM Category AS c " +
                            "INNER JOIN Product AS p " +
                            "ON p.CategoryID = c.CategoryID " +
                            "GROUP BY " +
                            "c.CategoryID, " +
                            "c.CategoryName " +
                            "ORDER BY " +
                            "c.CategoryName COLLATE NOCASE ASC";

            Log.d(
                    TAG,
                    "Thực hiện báo cáo giá theo danh mục."
            );

            cursor = database.rawQuery(
                    sql,
                    null
            );

            int categoryIDIndex =
                    cursor.getColumnIndexOrThrow(
                            "CategoryID"
                    );

            int categoryNameIndex =
                    cursor.getColumnIndexOrThrow(
                            "CategoryName"
                    );

            int averagePriceIndex =
                    cursor.getColumnIndexOrThrow(
                            "AveragePrice"
                    );

            int lowestPriceIndex =
                    cursor.getColumnIndexOrThrow(
                            "LowestPrice"
                    );

            int highestPriceIndex =
                    cursor.getColumnIndexOrThrow(
                            "HighestPrice"
                    );

            while (cursor.moveToNext()) {

                CategoryPriceReport report =
                        new CategoryPriceReport(
                                cursor.getInt(
                                        categoryIDIndex
                                ),

                                cursor.getString(
                                        categoryNameIndex
                                ),

                                cursor.getDouble(
                                        averagePriceIndex
                                ),

                                cursor.getLong(
                                        lowestPriceIndex
                                ),

                                cursor.getLong(
                                        highestPriceIndex
                                )
                        );

                reports.add(report);
            }

            Log.d(
                    TAG,
                    "Số danh mục trong báo cáo: "
                            + reports.size()
            );

        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (database != null) {
                database.close();
            }
        }

        return reports;
    }
    /**
     * Câu 9:
     * Lấy Top 10 sản phẩm có số tiền giảm giá cao nhất
     * và có điểm đánh giá từ 4.0 trở lên.
     */
    public static ArrayList<PotentialProductReport>
    getTopPotentialProducts(
            Context context
    ) throws IOException {

        ArrayList<PotentialProductReport> reports =
                new ArrayList<>();

        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = DatabaseHelper.openDatabase(
                    context
            );

            String sql =
                    "SELECT " +
                            "p.ProductID, " +
                            "p.ProductName, " +
                            "p.OriginalPrice AS OldPrice, " +
                            "p.PromotionalPrice AS NewPrice, " +
                            "(p.OriginalPrice - " +
                            "p.PromotionalPrice) AS AmountSaved, " +
                            "p.Rating " +
                            "FROM Product AS p " +
                            "WHERE p.Rating >= ? " +
                            "AND p.OriginalPrice > " +
                            "p.PromotionalPrice " +
                            "ORDER BY " +
                            "AmountSaved DESC, " +
                            "p.Rating DESC, " +
                            "p.ProductID ASC " +
                            "LIMIT ?";

            String[] arguments = {
                    "4.0",
                    "10"
            };

            Log.d(
                    TAG,
                    "Thực hiện truy vấn Top 10 " +
                            "sản phẩm tiềm năng."
            );

            cursor = database.rawQuery(
                    sql,
                    arguments
            );

            int productIDIndex =
                    cursor.getColumnIndexOrThrow(
                            "ProductID"
                    );

            int productNameIndex =
                    cursor.getColumnIndexOrThrow(
                            "ProductName"
                    );

            int oldPriceIndex =
                    cursor.getColumnIndexOrThrow(
                            "OldPrice"
                    );

            int newPriceIndex =
                    cursor.getColumnIndexOrThrow(
                            "NewPrice"
                    );

            int amountSavedIndex =
                    cursor.getColumnIndexOrThrow(
                            "AmountSaved"
                    );

            int ratingIndex =
                    cursor.getColumnIndexOrThrow(
                            "Rating"
                    );

            while (cursor.moveToNext()) {

                PotentialProductReport report =
                        new PotentialProductReport(
                                cursor.getInt(
                                        productIDIndex
                                ),

                                cursor.getString(
                                        productNameIndex
                                ),

                                cursor.getLong(
                                        oldPriceIndex
                                ),

                                cursor.getLong(
                                        newPriceIndex
                                ),

                                cursor.getLong(
                                        amountSavedIndex
                                ),

                                cursor.getDouble(
                                        ratingIndex
                                )
                        );

                reports.add(report);
            }

            Log.d(
                    TAG,
                    "Số sản phẩm tiềm năng: "
                            + reports.size()
            );

        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (database != null) {
                database.close();
            }
        }

        return reports;
    }
}