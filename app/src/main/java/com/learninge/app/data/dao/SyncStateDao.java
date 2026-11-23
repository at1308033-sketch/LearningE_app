package com.learninge.app.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.DbContract.SyncState;

import java.util.UUID;

/**
 * Bảng {@code sync_state} và số bản ghi chờ đẩy của mọi bảng đồng bộ.
 */
public final class SyncStateDao {
    // Từ của người dùng đếm theo chủ bộ; các bảng khác có sẵn user_id.
    private static final String COUNT_DIRTY = "SELECT "
            + "(SELECT COUNT(*) FROM user_settings WHERE user_id = ?1 AND dirty = 1) + "
            + "(SELECT COUNT(*) FROM decks WHERE owner_user_id = ?1 AND dirty = 1) + "
            + "(SELECT COUNT(*) FROM words w JOIN decks d ON d.id = w.deck_id "
            + "WHERE d.owner_user_id = ?1 AND w.dirty = 1) + "
            + "(SELECT COUNT(*) FROM card_progress WHERE user_id = ?1 AND dirty = 1) + "
            + "(SELECT COUNT(*) FROM favorites WHERE user_id = ?1 AND dirty = 1) + "
            + "(SELECT COUNT(*) FROM study_log WHERE user_id = ?1 AND dirty = 1) + "
            + "(SELECT COUNT(*) FROM quiz_attempts WHERE user_id = ?1 AND dirty = 1) + "
            + "(SELECT COUNT(*) FROM exam_attempts WHERE user_id = ?1 AND dirty = 1) + "
            + "(SELECT COUNT(*) FROM xp_events WHERE user_id = ?1 AND dirty = 1)";

    private final DatabaseHelper database;

    public SyncStateDao(DatabaseHelper database) {
        this.database = database;
    }

    /**
     * Cấp mã máy cho người dùng ở lần đăng nhập đầu trên máy này.
     */
    public void insertIfAbsent(long userId) {
        ContentValues values = new ContentValues();
        values.put(SyncState.USER_ID, userId);
        values.put(SyncState.DEVICE_ID, UUID.randomUUID().toString());
        database.getWritableDatabase().insertWithOnConflict(SyncState.TABLE, null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    public int countDirty(long userId) {
        try (Cursor cursor = database.getReadableDatabase().rawQuery(COUNT_DIRTY,
                new String[]{String.valueOf(userId)})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }
}
