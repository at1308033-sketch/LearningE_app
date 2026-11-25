package com.learninge.app.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.model.SyncRecord;
import com.learninge.app.data.model.SyncType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bản ghi chờ đẩy ({@code dirty = 1}) và việc xóa cờ sau khi server xác nhận (BR-064).
 */
public final class SyncOutbox {
    private static final String WORD_ID = "word_id = (SELECT id FROM words WHERE uuid = ?)";
    private static final Map<SyncType, String> SELECT = Map.of(
            SyncType.SETTINGS, "SELECT '' AS k, updated_at AS u, 0 AS d, level, daily_goal FROM user_settings "
                    + "WHERE user_id = ? AND dirty = 1",
            SyncType.DECK, "SELECT uuid AS k, updated_at AS u, deleted AS d, name, description, language_code, icon, "
                    + "is_default, sort_order, created_at FROM decks WHERE owner_user_id = ? AND dirty = 1",
            SyncType.WORD, "SELECT w.uuid AS k, w.updated_at AS u, w.deleted AS d, d.uuid AS deck_uuid, w.term, "
                    + "w.reading, w.part_of_speech, w.meaning_vi, w.example, w.example_translation, w.sort_order, "
                    + "w.created_at FROM words w JOIN decks d ON d.id = w.deck_id "
                    + "WHERE d.owner_user_id = ? AND w.dirty = 1",
            SyncType.CARD_PROGRESS, "SELECT w.uuid AS k, p.updated_at AS u, 0 AS d, p.box, p.next_review_date, "
                    + "p.first_learned_date, p.last_reviewed_at, p.review_count FROM card_progress p "
                    + "JOIN words w ON w.id = p.word_id WHERE p.user_id = ? AND p.dirty = 1",
            SyncType.FAVORITE, "SELECT w.uuid AS k, f.updated_at AS u, f.deleted AS d, f.created_at FROM favorites f "
                    + "JOIN words w ON w.id = f.word_id WHERE f.user_id = ? AND f.dirty = 1",
            SyncType.STUDY_LOG, "SELECT uuid AS k, updated_at AS u, 0 AS d, activity_type, ref_uuid, study_date, "
                    + "created_at FROM study_log WHERE user_id = ? AND dirty = 1",
            SyncType.QUIZ_ATTEMPT, "SELECT q.uuid AS k, q.updated_at AS u, 0 AS d, q.source_type, d.uuid AS deck_ref, "
                    + "q.score, q.correct_count, q.total_count, q.duration_sec, q.created_at FROM quiz_attempts q "
                    + "LEFT JOIN decks d ON d.id = q.deck_id WHERE q.user_id = ? AND q.dirty = 1",
            SyncType.EXAM_ATTEMPT, "SELECT a.uuid AS k, a.updated_at AS u, 0 AS d, t.code AS test_code, a.score, "
                    + "a.correct_count, a.total_count, a.duration_sec, a.started_at, a.submitted_at "
                    + "FROM exam_attempts a JOIN exam_tests t ON t.id = a.test_id WHERE a.user_id = ? AND a.dirty = 1",
            SyncType.XP_EVENT, "SELECT uuid AS k, updated_at AS u, 0 AS d, source_type, ref_uuid, correct_count, "
                    + "wrong_count, total_count, occurred_at FROM xp_events WHERE user_id = ? AND dirty = 1");
    private static final String[] QUEUE_ALL = {
            // updated_at = 0: cài đặt mặc định chưa ai sửa, máy chủ không nhận.
            "UPDATE user_settings SET dirty = 1 WHERE user_id = ? AND updated_at > 0",
            "UPDATE decks SET dirty = 1 WHERE owner_user_id = ?",
            "UPDATE words SET dirty = 1 WHERE deck_id IN (SELECT id FROM decks WHERE owner_user_id = ?)",
            "UPDATE card_progress SET dirty = 1 WHERE user_id = ?",
            "UPDATE favorites SET dirty = 1 WHERE user_id = ?",
            "UPDATE study_log SET dirty = 1 WHERE user_id = ?",
            "UPDATE quiz_attempts SET dirty = 1 WHERE user_id = ?",
            "UPDATE exam_attempts SET dirty = 1 WHERE user_id = ? AND status = 'SUBMITTED'",
            "UPDATE xp_events SET dirty = 1 WHERE user_id = ?"
    };
    private static final int META_COLUMNS = 3;

    private final DatabaseHelper database;

    public SyncOutbox(DatabaseHelper database) {
        this.database = database;
    }

    /**
     * Lô kế tiếp: cha trước con, trong mỗi loại theo thời điểm sửa.
     */
    public List<SyncRecord> next(long userId, int limit) {
        List<SyncRecord> batch = new ArrayList<>();
        for (SyncType type : SyncType.values()) {
            int room = limit - batch.size();
            if (room > 0) {
                batch.addAll(read(type, userId, room));
            }
        }
        return batch;
    }

    /**
     * Xóa cờ chỉ khi bản ghi không bị sửa tiếp trong lúc đẩy (so {@code updated_at}).
     */
    public void confirm(long userId, SyncRecord sent, long serverSeq) {
        ContentValues values = new ContentValues();
        values.put("dirty", 0);
        if (sent.type() != SyncType.SETTINGS && serverSeq > 0) {
            values.put("server_seq", serverSeq);
        }
        SQLiteDatabase db = database.getWritableDatabase();
        String updatedAt = String.valueOf(sent.updatedAt());
        String user = String.valueOf(userId);
        switch (sent.type()) {
            case SETTINGS -> db.update("user_settings", values, "user_id = ? AND updated_at = ?",
                    new String[]{user, updatedAt});
            case CARD_PROGRESS -> db.update("card_progress", values, "user_id = ? AND " + WORD_ID
                    + " AND updated_at = ?", new String[]{user, sent.key(), updatedAt});
            case FAVORITE -> db.update("favorites", values, "user_id = ? AND " + WORD_ID + " AND updated_at = ?",
                    new String[]{user, sent.key(), updatedAt});
            default -> db.update(table(sent.type()), values, "uuid = ? AND updated_at = ?",
                    new String[]{sent.key(), updatedAt});
        }
    }

    /**
     * Sau lần kéo đầu tiên mà server vẫn chưa có bộ mặc định thì đẩy bản trên máy lên, cùng mã tất định (BR-046).
     */
    public void queueMissingNotebook(long userId) {
        database.getWritableDatabase().execSQL("UPDATE decks SET dirty = 1 WHERE owner_user_id = ? AND is_default = 1 "
                + "AND server_seq IS NULL", new Object[]{userId});
    }

    /**
     * Đánh dấu chờ đẩy mọi bản ghi của người dùng, kể cả bản đã xóa.
     */
    public void queueAll(long userId) {
        for (String update : QUEUE_ALL) {
            database.getWritableDatabase().execSQL(update, new Object[]{userId});
        }
    }

    static String table(SyncType type) {
        return switch (type) {
            case DECK -> "decks";
            case WORD -> "words";
            case STUDY_LOG -> "study_log";
            case QUIZ_ATTEMPT -> "quiz_attempts";
            case EXAM_ATTEMPT -> "exam_attempts";
            case XP_EVENT -> "xp_events";
            case SETTINGS -> "user_settings";
            case CARD_PROGRESS -> "card_progress";
            case FAVORITE -> "favorites";
        };
    }

    private List<SyncRecord> read(SyncType type, long userId, int limit) {
        List<SyncRecord> records = new ArrayList<>();
        try (Cursor cursor = database.getReadableDatabase().rawQuery(SELECT.get(type) + " ORDER BY u LIMIT " + limit,
                new String[]{String.valueOf(userId)})) {
            while (cursor.moveToNext()) {
                Map<String, Object> fields = new HashMap<>();
                for (int i = META_COLUMNS; i < cursor.getColumnCount(); i++) {
                    fields.put(cursor.getColumnName(i), value(cursor, i));
                }
                records.add(new SyncRecord(type, cursor.getString(0), cursor.getLong(1), cursor.getInt(2) == 1, 0,
                        fields));
            }
        }
        return records;
    }

    private static Object value(Cursor cursor, int column) {
        return switch (cursor.getType(column)) {
            case Cursor.FIELD_TYPE_NULL -> null;
            case Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(column);
            default -> cursor.getString(column);
        };
    }
}
