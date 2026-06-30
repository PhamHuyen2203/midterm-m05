package com.example.dals;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.models.Product;

import java.io.IOException;
import java.util.ArrayList;

public final class ProductDAO {

    private static final String TAG =
            "ProductDAO";

    private ProductDAO() {
    }

    /**
     * Lấy một trang sản phẩm.
     *
     * Lần 1: LIMIT 10 OFFSET 0
     * Lần 2: LIMIT 10 OFFSET 10
     * Lần 3: LIMIT 10 OFFSET 20
     */
    public static ArrayList<Product> getProductsPage(
            Context context,
            int limit,
            int offset
    ) throws IOException {

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "LIMIT phải lớn hơn 0."
            );
        }

        if (offset < 0) {
            throw new IllegalArgumentException(
                    "OFFSET không được nhỏ hơn 0."
            );
        }

        ArrayList<Product> products =
                new ArrayList<>();

        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database =
                    DatabaseHelper.openDatabase(
                            context
                    );

            String sql =
                    "SELECT "
                            + "ProductID, "
                            + "TikiProductID, "
                            + "CategoryID, "
                            + "ProductName, "
                            + "OriginalPrice, "
                            + "PromotionalPrice, "
                            + "ImageURL, "
                            + "ProductURL, "
                            + "Description, "
                            + "Rating, "
                            + "CreatedAt "
                            + "FROM Product "
                            + "ORDER BY ProductID ASC "
                            + "LIMIT ? OFFSET ?";

            String[] arguments = {
                    String.valueOf(limit),
                    String.valueOf(offset)
            };

            Log.d(
                    TAG,
                    "Truy vấn Product: LIMIT="
                            + limit
                            + ", OFFSET="
                            + offset
            );

            cursor = database.rawQuery(
                    sql,
                    arguments
            );

            int productIDIndex =
                    cursor.getColumnIndexOrThrow(
                            "ProductID"
                    );

            int tikiProductIDIndex =
                    cursor.getColumnIndexOrThrow(
                            "TikiProductID"
                    );

            int categoryIDIndex =
                    cursor.getColumnIndexOrThrow(
                            "CategoryID"
                    );

            int productNameIndex =
                    cursor.getColumnIndexOrThrow(
                            "ProductName"
                    );

            int originalPriceIndex =
                    cursor.getColumnIndexOrThrow(
                            "OriginalPrice"
                    );

            int promotionalPriceIndex =
                    cursor.getColumnIndexOrThrow(
                            "PromotionalPrice"
                    );

            int imageURLIndex =
                    cursor.getColumnIndexOrThrow(
                            "ImageURL"
                    );

            int productURLIndex =
                    cursor.getColumnIndexOrThrow(
                            "ProductURL"
                    );

            int descriptionIndex =
                    cursor.getColumnIndexOrThrow(
                            "Description"
                    );

            int ratingIndex =
                    cursor.getColumnIndexOrThrow(
                            "Rating"
                    );

            int createdAtIndex =
                    cursor.getColumnIndexOrThrow(
                            "CreatedAt"
                    );

            while (cursor.moveToNext()) {

                Product product =
                        new Product(
                                cursor.getInt(
                                        productIDIndex
                                ),

                                cursor.getLong(
                                        tikiProductIDIndex
                                ),

                                cursor.getInt(
                                        categoryIDIndex
                                ),

                                cursor.getString(
                                        productNameIndex
                                ),

                                cursor.getLong(
                                        originalPriceIndex
                                ),

                                cursor.getLong(
                                        promotionalPriceIndex
                                ),

                                cursor.getString(
                                        imageURLIndex
                                ),

                                cursor.getString(
                                        productURLIndex
                                ),

                                cursor.getString(
                                        descriptionIndex
                                ),

                                cursor.getDouble(
                                        ratingIndex
                                ),

                                cursor.getString(
                                        createdAtIndex
                                )
                        );

                products.add(product);
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (database != null) {
                database.close();
            }
        }

        return products;
    }
}