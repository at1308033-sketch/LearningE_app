package com.learninge.app.data.dao;

import android.database.Cursor;

import com.learninge.app.data.db.DatabaseHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Truy vấn tổng hợp cho màn Tiến độ.
 */
public final class StatisticsDao {
    private static final int BOX_COUNT = 5;

    private final DatabaseHelper database;

    public StatisticsDao(DatabaseHelper database) {
        this.database = database;
    }

    /**
     * Tổng số từ người dùng học được: bộ có sẵn và bộ của chính họ.
     */
    public int countWords(long userId) {
        try (Cursor cursor = query("SELECT COUNT(*) FROM words w JOIN decks d ON d.id = w.deck_id "
                + "WHERE w.deleted = 0 AND d.deleted = 0 "
                + "AND (d.owner_user_id IS NULL OR d.owner_user_id = ?)", userId)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    /**
     * Số thẻ ở hộp 1..5 (BR-071).
     */
    public List<Integer> countByBox(long userId) {
        Integer[] counts = new Integer[BOX_COUNT];
        Arrays.fill(counts, 0);
        try (Cursor cursor = query("SELECT p.box, COUNT(*) FROM card_progress p "
                + "JOIN words w ON w.id = p.word_id AND w.deleted = 0 WHERE p.user_id = ? GROUP BY p.box", userId)) {
            while (cursor.moveToNext()) {
                counts[cursor.getInt(0) - 1] = cursor.getInt(1);
            }
        }
        return List.of(counts);
    }

    /**
     * Số hoạt động học theo ngày, từ {@code fromDay} trở đi.
     */
    public Map<String, Integer> countActivities(long userId, String fromDay) {
        Map<String, Integer> result = new HashMap<>();
        try (Cursor cursor = query("SELECT study_date, COUNT(*) FROM study_log WHERE user_id = ? AND study_date >= ? "
                + "GROUP BY study_date", userId, fromDay)) {
            while (cursor.moveToNext()) {
                result.put(cursor.getString(0), cursor.getInt(1));
            }
        }
        return result;
    }

    /**
     * Điểm quiz trung bình từ thời điểm đã cho; null nếu không có lượt nào.
     */
    public Integer averageQuizScore(long userId, long sinceMillis) {
        try (Cursor cursor = query("SELECT CAST(ROUND(AVG(score)) AS INTEGER) FROM quiz_attempts "
                + "WHERE user_id = ? AND created_at >= CAST(? AS INTEGER)", userId, String.valueOf(sinceMillis))) {
            return cursor.moveToFirst() && !cursor.isNull(0) ? Integer.valueOf(cursor.getInt(0)) : null;
        }
    }

    private Cursor query(String sql, long userId, String... more) {
        String[] args = new String[more.length + 1];
        args[0] = String.valueOf(userId);
        System.arraycopy(more, 0, args, 1, more.length);
        return database.getReadableDatabase().rawQuery(sql, args);
    }
}
