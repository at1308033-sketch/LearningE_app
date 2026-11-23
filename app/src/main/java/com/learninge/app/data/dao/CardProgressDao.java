package com.learninge.app.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.DbContract.CardProgress;
import com.learninge.app.data.db.DbContract.Sync;

import java.time.LocalDate;

/**
 * Bảng {@code card_progress}. Thẻ của từ đã xóa không được tính.
 */
public final class CardProgressDao {
    private static final String LIVE = "FROM card_progress p JOIN words w ON w.id = p.word_id AND w.deleted = 0 "
            + "WHERE p.user_id = ? AND ";

    private static final String KEY = CardProgress.USER_ID + " = ? AND " + CardProgress.WORD_ID + " = ?";

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

    /**
     * Hộp hiện tại, null = thẻ Mới.
     */
    public Integer findBox(long userId, long wordId) {
        try (Cursor cursor = database.getReadableDatabase().query(CardProgress.TABLE, new String[]{CardProgress.BOX},
                KEY, key(userId, wordId), null, null, null)) {
            return cursor.moveToFirst() ? Integer.valueOf(cursor.getInt(0)) : null;
        }
    }

    /**
     * Không UPSERT (DEC-011): UPDATE, 0 dòng thì INSERT; nơi gọi mở giao dịch.
     */
    public void save(long userId, long wordId, int box, LocalDate nextReview, LocalDate today, long now) {
        SQLiteDatabase db = database.getWritableDatabase();
        db.execSQL("UPDATE card_progress SET box = ?, next_review_date = ?, last_reviewed_at = ?, "
                        + "review_count = review_count + 1, updated_at = ?, dirty = 1 WHERE user_id = ? AND word_id = ?",
                new Object[]{box, nextReview.toString(), now, now, userId, wordId});
        if (findBox(userId, wordId) != null) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(CardProgress.USER_ID, userId);
        values.put(CardProgress.WORD_ID, wordId);
        values.put(CardProgress.BOX, box);
        values.put(CardProgress.NEXT_REVIEW_DATE, nextReview.toString());
        values.put(CardProgress.FIRST_LEARNED_DATE, today.toString());
        values.put(CardProgress.LAST_REVIEWED_AT, now);
        values.put(CardProgress.REVIEW_COUNT, 1);
        values.put(Sync.UPDATED_AT, now);
        db.insertOrThrow(CardProgress.TABLE, null, values);
    }

    private static String[] key(long userId, long wordId) {
        return new String[]{String.valueOf(userId), String.valueOf(wordId)};
    }

    private int count(String sql, String... args) {
        try (Cursor cursor = database.getReadableDatabase().rawQuery(sql, args)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }
}
