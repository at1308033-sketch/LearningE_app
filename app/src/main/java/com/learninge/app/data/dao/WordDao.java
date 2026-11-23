package com.learninge.app.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.DbContract.Sync;
import com.learninge.app.data.db.DbContract.Words;
import com.learninge.app.data.model.Word;
import com.learninge.app.data.model.WordItem;
import com.learninge.app.domain.rules.DeckRules;
import com.learninge.app.domain.rules.TextNormalizer;
import com.learninge.app.domain.rules.WordInput;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Bảng {@code words} kèm tiến độ và yêu thích của người dùng.
 */
public final class WordDao {
    /**
     * Không loại trừ từ nào khi kiểm tra trùng.
     */
    public static final long NONE = -1;

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
     * Từ chưa học của bộ, theo thứ tự trong bộ.
     */
    public List<WordItem> listNew(long userId, long deckId, int limit) {
        return list("w.deck_id = CAST(?2 AS INTEGER) AND p.box IS NULL ORDER BY w.sort_order, w.id "
                + "LIMIT CAST(?3 AS INTEGER)", String.valueOf(userId), String.valueOf(deckId), String.valueOf(limit));
    }

    public List<WordItem> listFavorites(long userId) {
        return list("f.word_id IS NOT NULL ORDER BY f.created_at DESC, w.id", String.valueOf(userId));
    }

    public List<WordItem> listNewFavorites(long userId, int limit) {
        return list("f.word_id IS NOT NULL AND p.box IS NULL ORDER BY f.created_at, w.id LIMIT CAST(?2 AS INTEGER)",
                String.valueOf(userId), String.valueOf(limit));
    }

    /**
     * Thẻ đến hạn: ngày ôn sớm nhất trước, rồi hộp thấp trước (BR-013).
     */
    public List<WordItem> listDue(long userId, String today, int limit) {
        return list("p.next_review_date <= ?2 ORDER BY p.next_review_date, p.box, w.id LIMIT CAST(?3 AS INTEGER)",
                String.valueOf(userId), today, String.valueOf(limit));
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

    public boolean existsTerm(long deckId, String searchTerm, long exceptId) {
        try (Cursor cursor = database.getReadableDatabase().rawQuery("SELECT 1 FROM words WHERE deck_id = ? "
                        + "AND search_term = ? AND deleted = 0 AND id <> ?",
                new String[]{String.valueOf(deckId), searchTerm, String.valueOf(exceptId)})) {
            return cursor.moveToFirst();
        }
    }

    /**
     * Thêm vào cuối bộ.
     */
    public long insert(long deckId, WordInput input, String languageCode, long now) {
        ContentValues values = content(input, languageCode, now);
        values.put(Sync.UUID, UUID.randomUUID().toString());
        values.put(Words.DECK_ID, deckId);
        values.put(Words.SORT_ORDER, nextSortOrder(deckId));
        values.put(Words.CREATED_AT, now);
        return database.getWritableDatabase().insertOrThrow(Words.TABLE, null, values);
    }

    /**
     * Cho nhập hàng loạt: trùng từ trong bộ thì bỏ qua (dựa trên index duy nhất), trả true nếu đã thêm.
     */
    public boolean insertIfAbsent(long deckId, WordInput input, String languageCode, long now) {
        ContentValues values = content(input, languageCode, now);
        values.put(Sync.UUID, UUID.randomUUID().toString());
        values.put(Words.DECK_ID, deckId);
        values.put(Words.SORT_ORDER, nextSortOrder(deckId));
        values.put(Words.CREATED_AT, now);
        return database.getWritableDatabase().insertWithOnConflict(Words.TABLE, null, values,
                SQLiteDatabase.CONFLICT_IGNORE) != -1;
    }

    public void update(long wordId, WordInput input, String languageCode, long now) {
        database.getWritableDatabase().update(Words.TABLE, content(input, languageCode, now), Words.ID + " = ?",
                new String[]{String.valueOf(wordId)});
    }

    public void markDeleted(long wordId, long now) {
        database.getWritableDatabase().update(Words.TABLE, tombstone(now), Words.ID + " = ?",
                new String[]{String.valueOf(wordId)});
    }

    public void markDeletedByDeck(long deckId, long now) {
        database.getWritableDatabase().update(Words.TABLE, tombstone(now),
                Words.DECK_ID + " = ? AND " + Sync.DELETED + " = 0", new String[]{String.valueOf(deckId)});
    }

    private int nextSortOrder(long deckId) {
        try (Cursor cursor = database.getReadableDatabase().rawQuery("SELECT COALESCE(MAX(sort_order), 0) + 1 "
                + "FROM words WHERE deck_id = ?", new String[]{String.valueOf(deckId)})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 1;
        }
    }

    private static ContentValues tombstone(long now) {
        ContentValues values = new ContentValues();
        values.put(Sync.DELETED, 1);
        values.put(Sync.DIRTY, 1);
        values.put(Sync.UPDATED_AT, now);
        return values;
    }

    private static ContentValues content(WordInput input, String languageCode, long now) {
        String term = DeckRules.clean(input.term());
        String reading = DeckRules.clean(input.reading());
        String meaning = DeckRules.clean(input.meaning());
        ContentValues values = new ContentValues();
        values.put(Words.TERM, term);
        values.put(Words.READING, reading);
        values.put(Words.MEANING_VI, meaning);
        values.put(Words.EXAMPLE, DeckRules.clean(input.example()));
        values.put(Words.EXAMPLE_TRANSLATION, DeckRules.clean(input.exampleTranslation()));
        values.put(Words.SEARCH_TERM, TextNormalizer.searchKey(term, languageCode));
        values.put(Words.SEARCH_READING, reading == null ? null : TextNormalizer.stripMarks(reading));
        values.put(Words.SEARCH_MEANING, TextNormalizer.stripMarks(meaning));
        values.put(Sync.DIRTY, 1);
        values.put(Sync.UPDATED_AT, now);
        return values;
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
