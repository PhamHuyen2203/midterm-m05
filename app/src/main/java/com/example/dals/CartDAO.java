package com.example.dals;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.example.models.CartItem;

import java.io.IOException;
import java.util.ArrayList;

public final class CartDAO {

    private static final String TAG = "CartDAO";

    private CartDAO() {
    }

    public static final class AddToCartResult {

        private final boolean inserted;
        private final int quantity;

        public AddToCartResult(
                boolean inserted,
                int quantity
        ) {
            this.inserted = inserted;
            this.quantity = quantity;
        }

        public boolean isInserted() {
            return inserted;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    /**
     * Nếu sản phẩm chưa tồn tại trong giỏ hàng:
     * INSERT một dòng mới.
     *
     * Nếu đã tồn tại:
     * UPDATE tăng Quantity, không INSERT dòng mới.
     */
    public static AddToCartResult addOrIncreaseProduct(
            Context context,
            int userID,
            int productID
    ) throws IOException {

        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = DatabaseHelper.openDatabase(context);

            database.beginTransaction();

            String checkSql =
                    "SELECT CartID, Quantity " +
                            "FROM Cart " +
                            "WHERE UserID = ? " +
                            "AND ProductID = ? " +
                            "LIMIT 1";

            cursor = database.rawQuery(
                    checkSql,
                    new String[]{
                            String.valueOf(userID),
                            String.valueOf(productID)
                    }
            );

            boolean productExists = cursor.moveToFirst();

            int currentQuantity = 0;

            if (productExists) {
                currentQuantity = cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                "Quantity"
                        )
                );
            }

            cursor.close();
            cursor = null;

            if (productExists) {
                String updateSql =
                        "UPDATE Cart " +
                                "SET Quantity = Quantity + 1 " +
                                "WHERE UserID = ? " +
                                "AND ProductID = ?";

                SQLiteStatement updateStatement =
                        database.compileStatement(updateSql);

                updateStatement.bindLong(1, userID);
                updateStatement.bindLong(2, productID);

                int affectedRows =
                        updateStatement.executeUpdateDelete();

                if (affectedRows == 0) {
                    throw new IllegalStateException(
                            "Không thể cập nhật số lượng sản phẩm."
                    );
                }

                database.setTransactionSuccessful();

                Log.d(
                        TAG,
                        "UPDATE Cart: UserID="
                                + userID
                                + ", ProductID="
                                + productID
                );

                return new AddToCartResult(
                        false,
                        currentQuantity + 1
                );

            } else {
                String insertSql =
                        "INSERT INTO Cart " +
                                "(UserID, ProductID, Quantity) " +
                                "VALUES (?, ?, 1)";

                SQLiteStatement insertStatement =
                        database.compileStatement(insertSql);

                insertStatement.bindLong(1, userID);
                insertStatement.bindLong(2, productID);

                long insertedCartID =
                        insertStatement.executeInsert();

                if (insertedCartID == -1) {
                    throw new IllegalStateException(
                            "Không thể thêm sản phẩm vào giỏ hàng."
                    );
                }

                database.setTransactionSuccessful();

                Log.d(
                        TAG,
                        "INSERT Cart: UserID="
                                + userID
                                + ", ProductID="
                                + productID
                );

                return new AddToCartResult(
                        true,
                        1
                );
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (database != null) {
                if (database.inTransaction()) {
                    database.endTransaction();
                }

                database.close();
            }
        }
    }

    /**
     * Lấy danh sách sản phẩm trong giỏ hàng.
     */
    public static ArrayList<CartItem> getCartItems(
            Context context,
            int userID
    ) throws IOException {

        ArrayList<CartItem> cartItems =
                new ArrayList<>();

        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = DatabaseHelper.openDatabase(context);

            String sql =
                    "SELECT " +
                            "c.CartID, " +
                            "c.UserID, " +
                            "c.ProductID, " +
                            "c.Quantity, " +
                            "p.ProductName, " +
                            "p.ImageURL, " +
                            "p.PromotionalPrice " +
                            "FROM Cart AS c " +
                            "INNER JOIN Product AS p " +
                            "ON p.ProductID = c.ProductID " +
                            "WHERE c.UserID = ? " +
                            "ORDER BY c.CartID DESC";

            cursor = database.rawQuery(
                    sql,
                    new String[]{
                            String.valueOf(userID)
                    }
            );

            int cartIDIndex =
                    cursor.getColumnIndexOrThrow(
                            "CartID"
                    );

            int userIDIndex =
                    cursor.getColumnIndexOrThrow(
                            "UserID"
                    );

            int productIDIndex =
                    cursor.getColumnIndexOrThrow(
                            "ProductID"
                    );

            int quantityIndex =
                    cursor.getColumnIndexOrThrow(
                            "Quantity"
                    );

            int productNameIndex =
                    cursor.getColumnIndexOrThrow(
                            "ProductName"
                    );

            int imageURLIndex =
                    cursor.getColumnIndexOrThrow(
                            "ImageURL"
                    );

            int priceIndex =
                    cursor.getColumnIndexOrThrow(
                            "PromotionalPrice"
                    );

            while (cursor.moveToNext()) {
                CartItem cartItem = new CartItem(
                        cursor.getInt(cartIDIndex),
                        cursor.getInt(userIDIndex),
                        cursor.getInt(productIDIndex),
                        cursor.getString(productNameIndex),
                        cursor.getString(imageURLIndex),
                        cursor.getLong(priceIndex),
                        cursor.getInt(quantityIndex)
                );

                cartItems.add(cartItem);
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (database != null) {
                database.close();
            }
        }

        return cartItems;
    }

    /**
     * UPDATE tăng số lượng.
     */
    public static boolean increaseQuantity(
            Context context,
            int userID,
            int cartID
    ) throws IOException {

        SQLiteDatabase database =
                DatabaseHelper.openDatabase(context);

        try {
            String sql =
                    "UPDATE Cart " +
                            "SET Quantity = Quantity + 1 " +
                            "WHERE CartID = ? " +
                            "AND UserID = ?";

            SQLiteStatement statement =
                    database.compileStatement(sql);

            statement.bindLong(1, cartID);
            statement.bindLong(2, userID);

            int affectedRows =
                    statement.executeUpdateDelete();

            Log.d(
                    TAG,
                    "Increase Quantity: CartID="
                            + cartID
                            + ", affectedRows="
                            + affectedRows
            );

            return affectedRows > 0;

        } finally {
            database.close();
        }
    }

    /**
     * UPDATE giảm số lượng.
     *
     * Chỉ giảm khi Quantity > 1 để không vi phạm
     * CHECK (Quantity > 0).
     */
    public static boolean decreaseQuantity(
            Context context,
            int userID,
            int cartID
    ) throws IOException {

        SQLiteDatabase database =
                DatabaseHelper.openDatabase(context);

        try {
            String sql =
                    "UPDATE Cart " +
                            "SET Quantity = Quantity - 1 " +
                            "WHERE CartID = ? " +
                            "AND UserID = ? " +
                            "AND Quantity > 1";

            SQLiteStatement statement =
                    database.compileStatement(sql);

            statement.bindLong(1, cartID);
            statement.bindLong(2, userID);

            int affectedRows =
                    statement.executeUpdateDelete();

            Log.d(
                    TAG,
                    "Decrease Quantity: CartID="
                            + cartID
                            + ", affectedRows="
                            + affectedRows
            );

            return affectedRows > 0;

        } finally {
            database.close();
        }
    }

    /**
     * DELETE sản phẩm khỏi giỏ hàng.
     */
    public static boolean deleteCartItem(
            Context context,
            int userID,
            int cartID
    ) throws IOException {

        SQLiteDatabase database =
                DatabaseHelper.openDatabase(context);

        try {
            String sql =
                    "DELETE FROM Cart " +
                            "WHERE CartID = ? " +
                            "AND UserID = ?";

            SQLiteStatement statement =
                    database.compileStatement(sql);

            statement.bindLong(1, cartID);
            statement.bindLong(2, userID);

            int affectedRows =
                    statement.executeUpdateDelete();

            Log.d(
                    TAG,
                    "DELETE Cart: CartID="
                            + cartID
                            + ", affectedRows="
                            + affectedRows
            );

            return affectedRows > 0;

        } finally {
            database.close();
        }
    }

    /**
     * Tổng số lượng sản phẩm trong giỏ.
     */
    public static int getTotalQuantity(
            Context context,
            int userID
    ) throws IOException {

        SQLiteDatabase database =
                DatabaseHelper.openDatabase(context);

        try {
            String sql =
                    "SELECT COALESCE(SUM(Quantity), 0) " +
                            "FROM Cart " +
                            "WHERE UserID = ?";

            SQLiteStatement statement =
                    database.compileStatement(sql);

            statement.bindLong(1, userID);

            return (int) statement.simpleQueryForLong();

        } finally {
            database.close();
        }
    }
}