package com.learninge.app.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.DbContract;
import com.learninge.app.data.db.DbContract.Sync;
import com.learninge.app.data.model.Settings;

/**
 * Bảng {@code user_settings}.
 */
public final class SettingsDao {
    private static final String[] COLUMNS = {DbContract.Settings.LEVEL, DbContract.Settings.DAILY_GOAL, DbContract.Settings.REMINDER_ENABLED,
            DbContract.Settings.REMINDER_MINUTE};

    private final DatabaseHelper database;

    public SettingsDao(DatabaseHelper database) {
        this.database = database;
    }

    /**
     * Tạo cài đặt mặc định (BR-006) nếu chưa có. {@code updated_at = 0} để bản trên máy chủ luôn thắng khi kéo về.
     */
    public void insertDefaults(long userId) {
        ContentValues values = new ContentValues();
        values.put(DbContract.Settings.USER_ID, userId);
        values.put(Sync.UPDATED_AT, 0);
        database.getWritableDatabase().insertWithOnConflict(DbContract.Settings.TABLE, null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    public Settings find(long userId) {
        try (Cursor cursor = database.getReadableDatabase().query(DbContract.Settings.TABLE, COLUMNS,
                DbContract.Settings.USER_ID + " = ?", new String[]{String.valueOf(userId)}, null, null, null)) {
            return cursor.moveToFirst() ? new Settings(cursor.getString(0),
                    cursor.getInt(1), cursor.getInt(2) == 1, cursor.getInt(3)) : null;
        }
    }
}
