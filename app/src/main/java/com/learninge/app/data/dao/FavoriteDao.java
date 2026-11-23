package com.learninge.app.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.DbContract.Favorites;
import com.learninge.app.data.db.DbContract.Sync;

/**
 * Bảng {@code favorites}: bỏ thích là đánh dấu xóa để máy khác cũng biết (BR-063).
 */
public final class FavoriteDao {
    private static final String KEY = Favorites.USER_ID + " = ? AND " + Favorites.WORD_ID + " = ?";

    private final DatabaseHelper database;

    public FavoriteDao(DatabaseHelper database) {
        this.database = database;
    }

    public boolean isFavorite(long userId, long wordId) {
        try (Cursor cursor = database.getReadableDatabase().query(Favorites.TABLE, new String[]{Sync.DELETED}, KEY,
                key(userId, wordId), null, null, null)) {
            return cursor.moveToFirst() && cursor.getInt(0) == 0;
        }
    }

    /**
     * UPDATE, 0 dòng thì INSERT; nơi gọi mở giao dịch.
     */
    public void set(long userId, long wordId, boolean isFavorite, long now) {
        SQLiteDatabase db = database.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Sync.DELETED, isFavorite ? 0 : 1);
        values.put(Sync.DIRTY, 1);
        values.put(Sync.UPDATED_AT, now);
        if (db.update(Favorites.TABLE, values, KEY, key(userId, wordId)) == 0) {
            values.put(Favorites.USER_ID, userId);
            values.put(Favorites.WORD_ID, wordId);
            values.put(Favorites.CREATED_AT, now);
            db.insertOrThrow(Favorites.TABLE, null, values);
        }
    }

    private static String[] key(long userId, long wordId) {
        return new String[]{String.valueOf(userId), String.valueOf(wordId)};
    }
}
