package com.learninge.app.data.dao;

import android.database.Cursor;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.model.Deck;
import com.learninge.app.data.model.DeckSummary;

import java.util.ArrayList;
import java.util.List;

/**
 * Bảng {@code decks}. Mọi truy vấn bỏ bộ đã xóa (tombstone).
 */
public final class DeckDao {
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
