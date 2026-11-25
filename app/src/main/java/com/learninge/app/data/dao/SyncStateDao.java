package com.learninge.app.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.DbContract;
import com.learninge.app.data.model.SyncState;

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

    private static final String BY_USER = DbContract.SyncState.USER_ID + " = ?";

    private final DatabaseHelper database;

    public SyncStateDao(DatabaseHelper database) {
        this.database = database;
    }

    /**
     * Cấp mã máy cho người dùng ở lần đăng nhập đầu trên máy này.
     */
    public void insertIfAbsent(long userId) {
        ContentValues values = new ContentValues();
        values.put(DbContract.SyncState.USER_ID, userId);
        values.put(DbContract.SyncState.DEVICE_ID, UUID.randomUUID().toString());
        database.getWritableDatabase().insertWithOnConflict(DbContract.SyncState.TABLE, null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    public SyncState find(long userId) {
        String[] columns = {DbContract.SyncState.DEVICE_ID, DbContract.SyncState.PULL_CURSOR,
                DbContract.SyncState.LAST_SYNC_AT, DbContract.SyncState.LAST_ERROR_CODE,
                DbContract.SyncState.LAST_ERROR_AT, DbContract.SyncState.DATA_EPOCH};
        try (Cursor cursor = database.getReadableDatabase().query(DbContract.SyncState.TABLE, columns, BY_USER,
                args(userId), null, null, null)) {
            return cursor.moveToFirst() ? new SyncState(cursor.getString(0), cursor.getLong(1),
                    cursor.isNull(2) ? null : cursor.getLong(2), cursor.getString(3),
                    cursor.isNull(4) ? null : cursor.getLong(4), cursor.getLong(5)) : null;
        }
    }

    public void saveCursor(long userId, long cursor) {
        ContentValues values = new ContentValues();
        values.put(DbContract.SyncState.PULL_CURSOR, cursor);
        update(userId, values);
    }

    public void saveEpoch(long userId, long epoch) {
        ContentValues values = new ContentValues();
        values.put(DbContract.SyncState.DATA_EPOCH, epoch);
        update(userId, values);
    }

    public void saveSuccess(long userId, long now) {
        ContentValues values = new ContentValues();
        values.put(DbContract.SyncState.LAST_SYNC_AT, now);
        values.put(DbContract.SyncState.LAST_ATTEMPT_AT, now);
        update(userId, values);
    }

    public void saveError(long userId, String code, long now) {
        ContentValues values = new ContentValues();
        values.put(DbContract.SyncState.LAST_ATTEMPT_AT, now);
        values.put(DbContract.SyncState.LAST_ERROR_CODE, code);
        values.put(DbContract.SyncState.LAST_ERROR_AT, now);
        update(userId, values);
    }

    private void update(long userId, ContentValues values) {
        database.getWritableDatabase().update(DbContract.SyncState.TABLE, values, BY_USER, args(userId));
    }

    private static String[] args(long userId) {
        return new String[]{String.valueOf(userId)};
    }

    public int countDirty(long userId) {
        try (Cursor cursor = database.getReadableDatabase().rawQuery(COUNT_DIRTY,
                new String[]{String.valueOf(userId)})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }
}
