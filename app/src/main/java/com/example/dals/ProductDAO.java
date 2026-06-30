package com.example.dals;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.models.Product;

import java.io.IOException;
import java.util.ArrayList;

public final class ProductDAO {

    private static final String TAG = "ProductDAO";

    private static final String PRODUCT_COLUMNS =
            "ProductID, "
                    + "TikiProductID, "
                    + "CategoryID, "
                    + "ProductName, "
                    + "OriginalPrice, "
                    + "PromotionalPrice, "
                    + "ImageURL, "
                    + "ProductURL, "
                    + "Description, "
                    + "Rating, "
                    + "CreatedAt ";

    private ProductDAO() {
    }

    /**
     * Câu 4:
     * Lấy danh sách sản phẩm thông thường theo trang.
     */
    public static ArrayList<Product> getProductsPage(
            Context context,
            int limit,
            int offset
    ) throws IOException {

        validatePagination(limit, offset);

        String sql =
                "SELECT "
                        + PRODUCT_COLUMNS
                        + "FROM Product "
                        + "ORDER BY ProductID ASC "
                        + "LIMIT ? OFFSET ?";

        String[] arguments = {
                String.valueOf(limit),
                String.valueOf(offset)
        };

        Log.d(
                TAG,
                "Lấy Product: LIMIT="
                        + limit
                        + ", OFFSET="
                        + offset
        );

        return executeProductQuery(
                context,
                sql,
                arguments
        );
    }

    /**
     * Câu 5:
     * Tìm theo tên sản phẩm và lọc theo khoảng giá.
     *
     * Điều kiện:
     * ProductName LIKE ?
     * AND PromotionalPrice BETWEEN ? AND ?
     *
     * Vẫn sử dụng LIMIT/OFFSET để giữ Infinite Scroll.
     */
    public static ArrayList<Product> searchProductsPage(
            Context context,
            String keyword,
            long minPrice,
            long maxPrice,
            int limit,
            int offset
    ) throws IOException {

        validatePagination(limit, offset);

        if (keyword == null
                || keyword.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Từ khóa tìm kiếm không được để trống."
            );
        }

        if (minPrice < 0 || maxPrice < 0) {
            throw new IllegalArgumentException(
                    "Giá không được nhỏ hơn 0."
            );
        }

        if (minPrice > maxPrice) {
            throw new IllegalArgumentException(
                    "Giá thấp nhất không được lớn hơn giá cao nhất."
            );
        }

        String sql =
                "SELECT "
                        + PRODUCT_COLUMNS
                        + "FROM Product "
                        + "WHERE ProductName LIKE ? "
                        + "AND PromotionalPrice BETWEEN ? AND ? "
                        + "ORDER BY ProductID ASC "
                        + "LIMIT ? OFFSET ?";

        /*
         * Ví dụ keyword = "sách"
         * thì giá trị thực tế truyền vào là "%sách%".
         */
        String[] arguments = {
                "%" + keyword.trim() + "%",
                String.valueOf(minPrice),
                String.valueOf(maxPrice),
                String.valueOf(limit),
                String.valueOf(offset)
        };

        Log.d(
                TAG,
                "Tìm Product: keyword="
                        + keyword
                        + ", minPrice="
                        + minPrice
                        + ", maxPrice="
                        + maxPrice
                        + ", LIMIT="
                        + limit
                        + ", OFFSET="
                        + offset
        );

        return executeProductQuery(
                context,
                sql,
                arguments
        );
    }

    /**
     * Thực hiện truy vấn và chuyển từng dòng Cursor
     * thành đối tượng Product.
     */
    private static ArrayList<Product> executeProductQuery(
            Context context,
            String sql,
            String[] arguments
    ) throws IOException {

        ArrayList<Product> products =
                new ArrayList<>();

        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = DatabaseHelper.openDatabase(
                    context
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

                Product product = new Product(
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

    private static void validatePagination(
            int limit,
            int offset
    ) {
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
    }
}