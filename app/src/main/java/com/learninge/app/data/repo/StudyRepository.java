package com.learninge.app.data.repo;

import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.data.dao.CardProgressDao;
import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.domain.rules.LeitnerScheduler;

import java.time.LocalDate;

/**
 * Ghi kết quả đánh giá thẻ (BR-011, BR-012, BR-015).
 */
public final class StudyRepository {
    private final DatabaseHelper database;
    private final CardProgressDao cards;

    public StudyRepository(DatabaseHelper database) {
        this.database = database;
        cards = new CardProgressDao(database);
    }

    /**
     * Trả hộp mới của thẻ.
     */
    public int rate(long userId, long wordId, boolean remembered, LocalDate today, long now) {
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        try {
            Integer current = cards.findBox(userId, wordId);
            int box = LeitnerScheduler.nextBox(current, remembered);
            cards.save(userId, wordId, box, LeitnerScheduler.nextReviewDate(box, today), today, now);
            db.setTransactionSuccessful();
            return box;
        } finally {
            db.endTransaction();
        }
    }
}
