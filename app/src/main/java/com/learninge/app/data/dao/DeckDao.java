package com.learninge.app.data.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.DbContract.Decks;
import com.learninge.app.data.db.DbContract.Sync;
import com.learninge.app.data.model.Deck;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.domain.rules.TextNormalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Bảng {@code decks}. Mọi truy vấn bỏ bộ đã xóa (tombstone).
 */
public final class DeckDao {
    /**
     * Không loại trừ bộ nào khi kiểm tra trùng tên.
     */
    public static final long NONE = -1;

    private static final String COLUMNS = "d.id, d.uuid, d.code, d.owner_user_id, d.name, d.description, "
            + "d.language_code, d.level, d.icon, d.exam_tag, d.is_default, d.created_at";
    private static final String SUMMARY = "SELECT " + COLUMNS + ", COUNT(w.id), "
            + "COALESCE(SUM(p.box = 5), 0), COALESCE(SUM(p.box < 5), 0), COALESCE(SUM(p.next_review_date <= ?2), 0) "
            + "FROM decks d LEFT JOIN words w ON w.deck_id = d.id AND w.deleted = 0 "
            + "LEFT JOIN card_progress p ON p.word_id = w.id AND p.user_id = ?1 WHERE d.deleted = 0 AND ";
    private static final String GROUP = " GROUP BY d.id ORDER BY d.is_default DESC, d.sort_order, d.id";

    private final DatabaseHelper database;

    public DeckDao(DatabaseHelper database) {
        this.database = database;
    }

    /**
     * Bộ có sẵn; {@code level}/{@code examTag} null = không lọc theo tiêu chí đó.
     */
    public List<DeckSummary> listBuiltin(long userId, String today, String level, String examTag) {
        // rawQuery không nhận tham số null nên dùng chuỗi rỗng cho "không lọc".
        return list("d.owner_user_id IS NULL AND (?3 = '' OR d.level = ?3) AND (?4 = '' OR d.exam_tag = ?4)",
                String.valueOf(userId), today, level == null ? "" : level, examTag == null ? "" : examTag);
    }

    public DeckSummary findSummary(long userId, String today, long deckId) {
        List<DeckSummary> found = list("d.id = CAST(?3 AS INTEGER) AND (d.owner_user_id IS NULL "
                + "OR d.owner_user_id = CAST(?1 AS INTEGER))", String.valueOf(userId), today, String.valueOf(deckId));
        return found.isEmpty() ? null : found.get(0);
    }

    /**
     * Bộ của người dùng, bộ mặc định đứng đầu.
     */
    public List<DeckSummary> listOwned(long userId, String today) {
        return list("d.owner_user_id = CAST(?1 AS INTEGER)", String.valueOf(userId), today);
    }

    public boolean existsName(long userId, String nameNorm, long exceptId) {
        try (Cursor cursor = database.getReadableDatabase().rawQuery("SELECT 1 FROM decks WHERE owner_user_id = ? "
                        + "AND name_norm = ? AND deleted = 0 AND id <> ?",
                new String[]{String.valueOf(userId), nameNorm, String.valueOf(exceptId)})) {
            return cursor.moveToFirst();
        }
    }

    public long insert(long userId, String name, String description, String languageCode, long now) {
        ContentValues values = content(name, description, languageCode, now);
        values.put(Sync.UUID, UUID.randomUUID().toString());
        values.put(Decks.OWNER_USER_ID, userId);
        values.put(Decks.CREATED_AT, now);
        return database.getWritableDatabase().insertOrThrow(Decks.TABLE, null, values);
    }

    public void update(long deckId, String name, String description, String languageCode, long now) {
        update(deckId, content(name, description, languageCode, now));
    }

    public void markDeleted(long deckId, long now) {
        ContentValues values = new ContentValues();
        values.put(Sync.DELETED, 1);
        values.put(Sync.DIRTY, 1);
        values.put(Sync.UPDATED_AT, now);
        update(deckId, values);
    }

    private void update(long deckId, ContentValues values) {
        database.getWritableDatabase().update(Decks.TABLE, values, Decks.ID + " = ?",
                new String[]{String.valueOf(deckId)});
    }

    private static ContentValues content(String name, String description, String languageCode, long now) {
        ContentValues values = new ContentValues();
        values.put(Decks.NAME, name);
        values.put(Decks.NAME_NORM, TextNormalizer.normalize(name));
        values.put(Decks.DESCRIPTION, description);
        values.put(Decks.LANGUAGE_CODE, languageCode);
        values.put(Sync.DIRTY, 1);
        values.put(Sync.UPDATED_AT, now);
        return values;
    }

    private List<DeckSummary> list(String where, String... args) {
        List<DeckSummary> result = new ArrayList<>();
        try (Cursor cursor = database.getReadableDatabase().rawQuery(SUMMARY + where + GROUP, args)) {
            while (cursor.moveToNext()) {
                int next = 12;
                result.add(new DeckSummary(read(cursor), cursor.getInt(next), cursor.getInt(next + 1),
                        cursor.getInt(next + 2), cursor.getInt(next + 3)));
            }
        }
        return result;
    }

    private static Deck read(Cursor cursor) {
        return new Deck(cursor.getLong(0), cursor.getString(1), cursor.getString(2),
                cursor.isNull(3) ? null : cursor.getLong(3), cursor.getString(4), cursor.getString(5),
                cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getString(9),
                cursor.getInt(10) == 1, cursor.getLong(11));
    }
}
