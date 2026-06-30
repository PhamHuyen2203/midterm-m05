package com.example.dals;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.io.IOException;

public final class UserDAO {

    private static final String DEMO_EMAIL =
            "customer@mcommerce.local";

    private UserDAO() {
    }

    public static int getOrCreateDemoCustomer(
            Context context
    ) throws IOException {

        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = DatabaseHelper.openDatabase(context);

            database.beginTransaction();

            String selectSql =
                    "SELECT UserID " +
                            "FROM \"User\" " +
                            "WHERE Email = ? " +
                            "LIMIT 1";

            cursor = database.rawQuery(
                    selectSql,
                    new String[]{DEMO_EMAIL}
            );

            if (cursor.moveToFirst()) {
                int userID = cursor.getInt(
                        cursor.getColumnIndexOrThrow(
                                "UserID"
                        )
                );

                database.setTransactionSuccessful();

                return userID;
            }

            cursor.close();
            cursor = null;

            String insertSql =
                    "INSERT INTO \"User\" " +
                            "(" +
                            "FullName, " +
                            "Email, " +
                            "Password, " +
                            "Address, " +
                            "Role" +
                            ") " +
                            "VALUES (?, ?, ?, ?, ?)";

            SQLiteStatement insertStatement =
                    database.compileStatement(insertSql);

            insertStatement.bindString(
                    1,
                    "Demo Customer"
            );

            insertStatement.bindString(
                    2,
                    DEMO_EMAIL
            );

            insertStatement.bindString(
                    3,
                    "123456"
            );

            insertStatement.bindString(
                    4,
                    "Ho Chi Minh City"
            );

            insertStatement.bindString(
                    5,
                    "Customer"
            );

            long insertedUserID =
                    insertStatement.executeInsert();

            if (insertedUserID == -1) {
                throw new IllegalStateException(
                        "Không thể tạo khách hàng demo."
                );
            }

            database.setTransactionSuccessful();

            return (int) insertedUserID;

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