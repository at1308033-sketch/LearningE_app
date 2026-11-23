package com.learninge.app.data.dao;

import android.database.Cursor;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.model.Word;
import com.learninge.app.data.model.WordItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Bảng {@code words} kèm tiến độ và yêu thích của người dùng.
 */
public final class WordDao {
    private static final String SELECT = "SELECT w.id, w.uuid, w.deck_id, w.term, w.reading, w.part_of_speech, "
            + "w.meaning_vi, w.example, w.example_translation, p.box, p.next_review_date, f.word_id IS NOT NULL "
            + "FROM words w LEFT JOIN card_progress p ON p.word_id = w.id AND p.user_id = ?1 "
            + "LEFT JOIN favorites f ON f.word_id = w.id AND f.user_id = ?1 AND f.deleted = 0 "
            + "WHERE w.deleted = 0 AND ";

    private final DatabaseHelper database;

    public WordDao(DatabaseHelper database) {
        this.database = database;
    }

    public List<WordItem> listByDeck(long userId, long deckId) {
        return list("w.deck_id = ?2 ORDER BY w.sort_order, w.id", String.valueOf(userId), String.valueOf(deckId));
    }

    /**
     * null nếu từ không tồn tại hoặc thuộc bộ của người khác.
     */
    public WordItem findById(long userId, long wordId) {
        List<WordItem> found = list("w.id = CAST(?2 AS INTEGER) AND EXISTS (SELECT 1 FROM decks d "
                + "WHERE d.id = w.deck_id AND d.deleted = 0 "
                + "AND (d.owner_user_id IS NULL OR d.owner_user_id = CAST(?1 AS INTEGER)))",
                String.valueOf(userId), String.valueOf(wordId));
        return found.isEmpty() ? null : found.get(0);
    }

    private List<WordItem> list(String where, String... args) {
        List<WordItem> result = new ArrayList<>();
        try (Cursor cursor = database.getReadableDatabase().rawQuery(SELECT + where, args)) {
            while (cursor.moveToNext()) {
                result.add(read(cursor));
            }
        }
        return result;
    }

    private static WordItem read(Cursor cursor) {
        Word word = new Word(cursor.getLong(0), cursor.getString(1), cursor.getLong(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7),
                cursor.getString(8));
        return new WordItem(word, cursor.isNull(9) ? null : cursor.getInt(9), cursor.getString(10),
                cursor.getInt(11) == 1);
    }
}
