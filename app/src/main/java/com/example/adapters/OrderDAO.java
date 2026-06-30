package com.example.dals;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.IOException;

public final class OrderDAO {

    private static final String TAG = "OrderDAO";

    private OrderDAO() {
    }

    /**
     * Kết quả trả về sau khi Checkout thành công.
     */
    public static final class CheckoutResult {

        private final long orderID;
        private final long totalAmount;
        private final int totalQuantity;
        private final int lineCount;

        public CheckoutResult(
                long orderID,
                long totalAmount,
                int totalQuantity,
                int lineCount
        ) {
            this.orderID = orderID;
            this.totalAmount = totalAmount;
            this.totalQuantity = totalQuantity;
            this.lineCount = lineCount;
        }

        public long getOrderID() {
            return orderID;
        }

        public long getTotalAmount() {
            return totalAmount;
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }

        public int getLineCount() {
            return lineCount;
        }
    }

    /**
     * Thực hiện Checkout trong cùng một transaction:
     *
     * 1. INSERT Orders.
     * 2. INSERT Cart sang OrderDetail.
     * 3. DELETE Cart.
     */
    public static CheckoutResult checkout(
            Context context,
            int userID,
            String shippingAddress,
            String paymentMethod
    ) throws IOException {

        if (userID <= 0) {
            throw new IllegalArgumentException(
                    "UserID không hợp lệ."
            );
        }

        if (shippingAddress == null
                || shippingAddress.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Địa chỉ giao hàng không được để trống."
            );
        }

        if (paymentMethod == null
                || paymentMethod.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Phương thức thanh toán không hợp lệ."
            );
        }

        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = DatabaseHelper.openDatabase(
                    context
            );

            /*
             * Bắt đầu transaction.
             * Nếu bất kỳ bước nào lỗi thì toàn bộ dữ liệu
             * sẽ được rollback.
             */
            database.beginTransaction();

            // =================================================
            // BƯỚC 1: ĐỌC VÀ TÍNH TỔNG GIỎ HÀNG
            // =================================================

            String cartSummarySql =
                    "SELECT " +
                            "COUNT(*) AS LineCount, " +
                            "COALESCE(SUM(c.Quantity), 0) " +
                            "AS TotalQuantity, " +
                            "COALESCE(SUM(" +
                            "c.Quantity * p.PromotionalPrice" +
                            "), 0) AS TotalAmount " +
                            "FROM Cart AS c " +
                            "INNER JOIN Product AS p " +
                            "ON p.ProductID = c.ProductID " +
                            "WHERE c.UserID = ?";

            cursor = database.rawQuery(
                    cartSummarySql,
                    new String[]{
                            String.valueOf(userID)
                    }
            );

            if (!cursor.moveToFirst()) {
                throw new IllegalStateException(
                        "Không thể đọc dữ liệu giỏ hàng."
                );
            }

            int lineCount = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            "LineCount"
                    )
            );

            int totalQuantity = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            "TotalQuantity"
                    )
            );

            long totalAmount = cursor.getLong(
                    cursor.getColumnIndexOrThrow(
                            "TotalAmount"
                    )
            );

            cursor.close();
            cursor = null;

            if (lineCount <= 0 || totalQuantity <= 0) {
                throw new IllegalStateException(
                        "Giỏ hàng đang trống."
                );
            }

            if (totalAmount <= 0) {
                throw new IllegalStateException(
                        "Tổng tiền đơn hàng không hợp lệ."
                );
            }

            // =================================================
            // BƯỚC 2: INSERT VÀO ORDERS
            // =================================================

            String insertOrderSql =
                    "INSERT INTO Orders " +
                            "(" +
                            "UserID, " +
                            "TotalAmount, " +
                            "ShippingAddress, " +
                            "PaymentMethod, " +
                            "PaymentStatus" +
                            ") " +
                            "VALUES (?, ?, ?, ?, ?)";

            SQLiteStatement insertOrderStatement =
                    database.compileStatement(
                            insertOrderSql
                    );

            insertOrderStatement.bindLong(
                    1,
                    userID
            );

            insertOrderStatement.bindLong(
                    2,
                    totalAmount
            );

            insertOrderStatement.bindString(
                    3,
                    shippingAddress.trim()
            );

            insertOrderStatement.bindString(
                    4,
                    paymentMethod.trim()
            );

            /*
             * Để Câu 10 có thể lấy các đơn đã thanh toán,
             * Checkout thành công sẽ lưu trạng thái Paid.
             */
            insertOrderStatement.bindString(
                    5,
                    "Paid"
            );

            long orderID =
                    insertOrderStatement.executeInsert();

            if (orderID == -1) {
                throw new IllegalStateException(
                        "Không thể tạo đơn hàng."
                );
            }

            Log.d(
                    TAG,
                    "INSERT Orders: OrderID="
                            + orderID
                            + ", TotalAmount="
                            + totalAmount
            );

            // =================================================
            // BƯỚC 3: CHUYỂN CART SANG ORDERDETAIL
            // =================================================

            /*
             * Một câu INSERT ... SELECT sẽ chuyển toàn bộ
             * sản phẩm trong Cart sang OrderDetail.
             *
             * UnitPrice được lấy từ PromotionalPrice.
             */
            String insertOrderDetailSql =
                    "INSERT INTO OrderDetail " +
                            "(" +
                            "OrderID, " +
                            "ProductID, " +
                            "Quantity, " +
                            "UnitPrice" +
                            ") " +
                            "SELECT " +
                            "?, " +
                            "c.ProductID, " +
                            "c.Quantity, " +
                            "p.PromotionalPrice " +
                            "FROM Cart AS c " +
                            "INNER JOIN Product AS p " +
                            "ON p.ProductID = c.ProductID " +
                            "WHERE c.UserID = ?";

            database.execSQL(
                    insertOrderDetailSql,
                    new Object[]{
                            orderID,
                            userID
                    }
            );

            /*
             * Kiểm tra số dòng OrderDetail vừa tạo
             * phải bằng số dòng Cart ban đầu.
             */
            String countOrderDetailSql =
                    "SELECT COUNT(*) " +
                            "FROM OrderDetail " +
                            "WHERE OrderID = ?";

            SQLiteStatement countDetailStatement =
                    database.compileStatement(
                            countOrderDetailSql
                    );

            countDetailStatement.bindLong(
                    1,
                    orderID
            );

            int insertedDetailCount =
                    (int) countDetailStatement
                            .simpleQueryForLong();

            if (insertedDetailCount != lineCount) {
                throw new IllegalStateException(
                        "Số chi tiết đơn hàng không khớp. " +
                                "Cart=" + lineCount +
                                ", OrderDetail="
                                + insertedDetailCount
                );
            }

            Log.d(
                    TAG,
                    "INSERT OrderDetail: OrderID="
                            + orderID
                            + ", rows="
                            + insertedDetailCount
            );

            // =================================================
            // BƯỚC 4: XÓA DỮ LIỆU CŨ TRONG CART
            // =================================================

            String deleteCartSql =
                    "DELETE FROM Cart " +
                            "WHERE UserID = ?";

            SQLiteStatement deleteCartStatement =
                    database.compileStatement(
                            deleteCartSql
                    );

            deleteCartStatement.bindLong(
                    1,
                    userID
            );

            int deletedCartRows =
                    deleteCartStatement
                            .executeUpdateDelete();

            if (deletedCartRows != lineCount) {
                throw new IllegalStateException(
                        "Không thể xóa toàn bộ giỏ hàng. " +
                                "Expected=" + lineCount +
                                ", Deleted=" + deletedCartRows
                );
            }

            Log.d(
                    TAG,
                    "DELETE Cart: UserID="
                            + userID
                            + ", rows="
                            + deletedCartRows
            );

            /*
             * Chỉ commit khi tất cả bước đều thành công.
             */
            database.setTransactionSuccessful();

            return new CheckoutResult(
                    orderID,
                    totalAmount,
                    totalQuantity,
                    lineCount
            );

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
}