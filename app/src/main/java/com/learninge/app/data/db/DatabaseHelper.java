package com.learninge.app.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * Tạo và nâng cấp CSDL app (DATABASE-DESIGN phần A). Lược đồ nằm ở {@link DbSchema}.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "learninge.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        this(context, DATABASE_NAME);
    }

    /**
     * {@code name = null} tạo CSDL trong bộ nhớ, dùng cho test.
     */
    public DatabaseHelper(Context context, @Nullable String name) {
        super(context, name, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String statement : DbSchema.V1) {
            db.execSQL(statement);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Chưa có phiên bản 2. Mỗi phiên bản mới thêm một bước ALTER/CREATE, không xóa bảng dữ liệu người dùng (NFR-E3).
    }
}
