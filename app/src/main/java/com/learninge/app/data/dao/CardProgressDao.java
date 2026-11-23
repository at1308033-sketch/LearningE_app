package com.learninge.app.data.dao;

import android.database.Cursor;

import com.learninge.app.data.db.DatabaseHelper;

/**
 * Bảng {@code card_progress}. Thẻ của từ đã xóa không được tính.
 */
public final class CardProgressDao {
    private static final String LIVE = "FROM card_progress p JOIN words w ON w.id = p.word_id AND w.deleted = 0 "
            + "WHERE p.user_id = ? AND ";

    private final DatabaseHelper database;

    public CardProgressDao(DatabaseHelper database) {
        this.database = database;
    }

    /**
     * Số thẻ đến hạn (BR-013).
     */
    public int countDue(long userId, String today) {
        return count("SELECT COUNT(*) " + LIVE + "p.next_review_date <= ?", String.valueOf(userId), today);
    }

    /**
     * Số từ mới đã học trong ngày, so với mục tiêu hằng ngày.
     */
    public int countLearnedOn(long userId, String day) {
        return count("SELECT COUNT(*) " + LIVE + "p.first_learned_date = ?", String.valueOf(userId), day);
    }

    private int count(String sql, String... args) {
        try (Cursor cursor = database.getReadableDatabase().rawQuery(sql, args)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }
}
