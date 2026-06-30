package com.example.dals;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.example.models.AdminOrderItem;

import java.io.IOException;
import java.util.ArrayList;

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
     * Câu 7:
     *
     * 1. INSERT vào Orders.
     * 2. Chuyển Cart sang OrderDetail.
     * 3. DELETE dữ liệu trong Cart.
     *
     * Tất cả thao tác nằm trong cùng một transaction.
     */
    public static CheckoutResult checkout(
            Context context,
            int userID,
            String customerName,
            String customerEmail,
            String shippingAddress,
            String paymentMethod
    ) throws IOException {

        if (userID <= 0) {
            throw new IllegalArgumentException(
                    "UserID không hợp lệ."
            );
        }

        if (customerName == null
                || customerName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Họ tên khách hàng không được để trống."
            );
        }

        if (customerEmail == null
                || customerEmail.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Email khách hàng không được để trống."
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

            database.beginTransaction();

            // =================================================
// BƯỚC 0: KIỂM TRA VÀ CẬP NHẬT USER
// =================================================

            String checkUserSql =
                    "SELECT COUNT(*) " +
                            "FROM \"User\" " +
                            "WHERE UserID = ?";

            SQLiteStatement checkUserStatement =
                    database.compileStatement(
                            checkUserSql
                    );

            checkUserStatement.bindLong(
                    1,
                    userID
            );

            long userCount =
                    checkUserStatement.simpleQueryForLong();

            if (userCount == 0) {
                throw new IllegalStateException(
                        "Không tìm thấy người dùng có UserID="
                                + userID
                );
            }

            String updateUserSql =
                    "UPDATE \"User\" " +
                            "SET FullName = ?, " +
                            "Email = ?, " +
                            "Address = ?, " +
                            "Role = ? " +
                            "WHERE UserID = ?";

            SQLiteStatement updateUserStatement =
                    database.compileStatement(
                            updateUserSql
                    );

            updateUserStatement.bindString(
                    1,
                    customerName.trim()
            );

            updateUserStatement.bindString(
                    2,
                    customerEmail.trim()
            );

            updateUserStatement.bindString(
                    3,
                    shippingAddress.trim()
            );

            updateUserStatement.bindString(
                    4,
                    "Customer"
            );

            updateUserStatement.bindLong(
                    5,
                    userID
            );

            int updatedUserRows =
                    updateUserStatement
                            .executeUpdateDelete();

            if (updatedUserRows != 1) {
                throw new IllegalStateException(
                        "Không thể cập nhật thông tin khách hàng."
                );
            }

            Log.d(
                    TAG,
                    "UPDATE User: UserID="
                            + userID
                            + ", FullName="
                            + customerName
            );

            // ================================================
            // BƯỚC 1: ĐỌC VÀ TÍNH TỔNG GIỎ HÀNG
            // ================================================

            String cartSummarySql =
                    "SELECT " +
                            "COUNT(*) AS LineCount, " +
                            "COALESCE(SUM(c.Quantity), 0) AS TotalQuantity, " +
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

            // ================================================
            // BƯỚC 2: INSERT VÀO ORDERS
            // ================================================

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

            // ================================================
            // BƯỚC 3: CHUYỂN CART SANG ORDERDETAIL
            // ================================================

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
                                ", OrderDetail=" +
                                insertedDetailCount
                );
            }

            Log.d(
                    TAG,
                    "INSERT OrderDetail: OrderID="
                            + orderID
                            + ", rows="
                            + insertedDetailCount
            );

            // ================================================
            // BƯỚC 4: XÓA CART
            // ================================================

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

    /**
     * Câu 10:
     * Lấy danh sách đơn hàng đã thanh toán.
     *
     * - JOIN Orders với User.
     * - JOIN OrderDetail để lấy số loại và tổng số lượng.
     * - Phân trang bằng LIMIT và OFFSET.
     */
    public static ArrayList<AdminOrderItem> getPaidOrdersPage(
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

        ArrayList<AdminOrderItem> orders =
                new ArrayList<>();

        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = DatabaseHelper.openDatabase(
                    context
            );

            String sql =
                    "SELECT " +
                            "o.OrderID AS OrderID, " +
                            "o.UserID AS UserID, " +
                            "u.FullName AS CustomerName, " +
                            "u.Email AS CustomerEmail, " +
                            "o.OrderDate AS OrderDate, " +
                            "o.TotalAmount AS TotalAmount, " +
                            "o.ShippingAddress AS ShippingAddress, " +
                            "o.PaymentMethod AS PaymentMethod, " +
                            "o.PaymentStatus AS PaymentStatus, " +
                            "COUNT(od.OrderDetailID) AS LineCount, " +
                            "COALESCE(SUM(od.Quantity), 0) " +
                            "AS TotalQuantity " +
                            "FROM Orders AS o " +
                            "INNER JOIN \"User\" AS u " +
                            "ON u.UserID = o.UserID " +
                            "LEFT JOIN OrderDetail AS od " +
                            "ON od.OrderID = o.OrderID " +
                            "WHERE o.PaymentStatus = ? " +
                            "GROUP BY " +
                            "o.OrderID, " +
                            "o.UserID, " +
                            "u.FullName, " +
                            "u.Email, " +
                            "o.OrderDate, " +
                            "o.TotalAmount, " +
                            "o.ShippingAddress, " +
                            "o.PaymentMethod, " +
                            "o.PaymentStatus " +
                            "ORDER BY o.OrderID DESC " +
                            "LIMIT ? OFFSET ?";

            String[] arguments = {
                    "Paid",
                    String.valueOf(limit),
                    String.valueOf(offset)
            };

            Log.d(
                    TAG,
                    "SELECT paid orders: LIMIT="
                            + limit
                            + ", OFFSET="
                            + offset
            );

            /*
             * Quan trọng:
             * Phải thực hiện rawQuery trước khi gọi
             * cursor.getColumnIndexOrThrow().
             */
            cursor = database.rawQuery(
                    sql,
                    arguments
            );

            if (cursor == null) {
                throw new IllegalStateException(
                        "SQLite không trả về Cursor."
                );
            }

            Log.d(
                    TAG,
                    "Columns: "
                            + java.util.Arrays.toString(
                            cursor.getColumnNames()
                    )
            );

            int orderIDIndex =
                    cursor.getColumnIndexOrThrow(
                            "OrderID"
                    );

            int userIDIndex =
                    cursor.getColumnIndexOrThrow(
                            "UserID"
                    );

            int customerNameIndex =
                    cursor.getColumnIndexOrThrow(
                            "CustomerName"
                    );

            int customerEmailIndex =
                    cursor.getColumnIndexOrThrow(
                            "CustomerEmail"
                    );

            int orderDateIndex =
                    cursor.getColumnIndexOrThrow(
                            "OrderDate"
                    );

            int totalAmountIndex =
                    cursor.getColumnIndexOrThrow(
                            "TotalAmount"
                    );

            int shippingAddressIndex =
                    cursor.getColumnIndexOrThrow(
                            "ShippingAddress"
                    );

            int paymentMethodIndex =
                    cursor.getColumnIndexOrThrow(
                            "PaymentMethod"
                    );

            int paymentStatusIndex =
                    cursor.getColumnIndexOrThrow(
                            "PaymentStatus"
                    );

            int lineCountIndex =
                    cursor.getColumnIndexOrThrow(
                            "LineCount"
                    );

            int totalQuantityIndex =
                    cursor.getColumnIndexOrThrow(
                            "TotalQuantity"
                    );

            while (cursor.moveToNext()) {

                AdminOrderItem order =
                        new AdminOrderItem(
                                cursor.getLong(
                                        orderIDIndex
                                ),

                                cursor.getInt(
                                        userIDIndex
                                ),

                                cursor.getString(
                                        customerNameIndex
                                ),

                                cursor.getString(
                                        customerEmailIndex
                                ),

                                cursor.getString(
                                        orderDateIndex
                                ),

                                cursor.getLong(
                                        totalAmountIndex
                                ),

                                cursor.getString(
                                        shippingAddressIndex
                                ),

                                cursor.getString(
                                        paymentMethodIndex
                                ),

                                cursor.getString(
                                        paymentStatusIndex
                                ),

                                cursor.getInt(
                                        lineCountIndex
                                ),

                                cursor.getInt(
                                        totalQuantityIndex
                                )
                        );

                orders.add(order);
            }

            Log.d(
                    TAG,
                    "Paid orders loaded: "
                            + orders.size()
            );

        } finally {
            if (cursor != null
                    && !cursor.isClosed()) {

                cursor.close();
            }

            if (database != null
                    && database.isOpen()) {

                database.close();
            }
        }

        return orders;
    }
}