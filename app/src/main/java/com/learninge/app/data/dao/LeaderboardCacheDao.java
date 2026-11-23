package com.learninge.app.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.DbContract.LeaderboardCache;

import java.time.LocalDate;

/**
 * Bảng {@code leaderboard_cache}: bản tải gần nhất để xem khi mất mạng, giữ 2 tuần.
 */
public final class LeaderboardCacheDao {

    /**
     * Bản đã lưu; {@code payload} là bytes của {@code GetWeeklyResponse}.
     */
    public record Cached(byte[] payload, long fetchedAt) {
    }

    private final DatabaseHelper database;

    public LeaderboardCacheDao(DatabaseHelper database) {
        this.database = database;
    }

    /**
     * Bản mới nhất, hoặc null.
     */
    public Cached findLatest(long userId) {
        try (Cursor cursor = database.getReadableDatabase().query(LeaderboardCache.TABLE,
                new String[]{LeaderboardCache.PAYLOAD, LeaderboardCache.FETCHED_AT}, LeaderboardCache.USER_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, LeaderboardCache.WEEK_START + " DESC", "1")) {
            return cursor.moveToFirst() ? new Cached(cursor.getBlob(0), cursor.getLong(1)) : null;
        }
    }

    public void save(long userId, LocalDate weekStart, byte[] payload, long now) {
        SQLiteDatabase db = database.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LeaderboardCache.USER_ID, userId);
        values.put(LeaderboardCache.WEEK_START, weekStart.toString());
        values.put(LeaderboardCache.PAYLOAD, payload);
        values.put(LeaderboardCache.FETCHED_AT, now);
        db.replaceOrThrow(LeaderboardCache.TABLE, null, values);
        db.delete(LeaderboardCache.TABLE, LeaderboardCache.USER_ID + " = ? AND " + LeaderboardCache.WEEK_START + " < ?",
                new String[]{String.valueOf(userId), weekStart.minusWeeks(1).toString()});
    }
}
