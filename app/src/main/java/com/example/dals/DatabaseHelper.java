package com.example.dals;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class DatabaseHelper {

    public static final String DATABASE_NAME =
            "MCommerce.sqlite";

    private static final int BUFFER_SIZE = 8192;

    private DatabaseHelper() {
    }

    /**
     * Copy database từ Assets vào thư mục database
     * nội bộ của ứng dụng trong lần chạy đầu tiên.
     */
    public static synchronized void copyDatabaseIfNeeded(
            Context context
    ) throws IOException {

        Context applicationContext =
                context.getApplicationContext();

        File databaseFile =
                applicationContext.getDatabasePath(
                        DATABASE_NAME
                );

        /*
         * Database đã tồn tại thì không copy lại.
         */
        if (databaseFile.exists()
                && databaseFile.length() > 0) {
            return;
        }

        File databaseDirectory =
                databaseFile.getParentFile();

        if (databaseDirectory != null
                && !databaseDirectory.exists()) {

            boolean created =
                    databaseDirectory.mkdirs();

            if (!created
                    && !databaseDirectory.exists()) {

                throw new IOException(
                        "Không thể tạo thư mục database."
                );
            }
        }

        try (
                InputStream inputStream =
                        applicationContext
                                .getAssets()
                                .open(DATABASE_NAME);

                OutputStream outputStream =
                        new FileOutputStream(databaseFile)
        ) {
            byte[] buffer =
                    new byte[BUFFER_SIZE];

            int length;

            while (
                    (length = inputStream.read(buffer))
                            > 0
            ) {
                outputStream.write(
                        buffer,
                        0,
                        length
                );
            }

            outputStream.flush();
        }
    }

    /**
     * Mở database đã được copy vào thiết bị.
     */
    public static SQLiteDatabase openDatabase(
            Context context
    ) throws IOException {

        copyDatabaseIfNeeded(context);

        File databaseFile =
                context.getApplicationContext()
                        .getDatabasePath(
                                DATABASE_NAME
                        );

        if (!databaseFile.exists()) {
            throw new IOException(
                    "Không tìm thấy database tại: "
                            + databaseFile.getAbsolutePath()
            );
        }

        SQLiteDatabase database =
                SQLiteDatabase.openDatabase(
                        databaseFile.getAbsolutePath(),
                        null,
                        SQLiteDatabase.OPEN_READWRITE
                );

        database.execSQL(
                "PRAGMA foreign_keys = ON"
        );

        return database;
    }
}