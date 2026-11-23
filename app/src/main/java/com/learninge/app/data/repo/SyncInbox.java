package com.learninge.app.data.repo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.model.SyncRecord;
import com.learninge.app.data.model.SyncType;
import com.learninge.app.domain.rules.TextNormalizer;

import java.time.Instant;
import java.time.ZoneId;

/**
 * Ghi bản ghi của server vào máy. Nơi gọi mở giao dịch.
 */
public final class SyncInbox {
    // BR-068: ngày tính XP theo giờ Việt Nam.
    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");

    private final DatabaseHelper database;

    public SyncInbox(DatabaseHelper database) {
        this.database = database;
    }

    /**
     * @param isForced true với kết quả STALE: server đã phân xử nên ghi đè kể cả khi bản trên máy còn chờ đẩy
     * @return false nếu thiếu bản ghi cha (bộ, từ, đề), cần thử lại sau khi trang khác được ghi
     */
    public boolean apply(long userId, SyncRecord record, boolean isForced) {
        return switch (record.type()) {
            case SETTINGS -> applySettings(userId, record, isForced);
            case DECK -> applyDeck(userId, record, isForced);
            case WORD -> applyWord(record, isForced);
            case CARD_PROGRESS -> applyCard(userId, record, isForced);
            case FAVORITE -> applyFavorite(userId, record, isForced);
            case STUDY_LOG -> applyStudyLog(userId, record);
            case QUIZ_ATTEMPT -> applyQuiz(userId, record);
            case EXAM_ATTEMPT -> applyExam(userId, record);
            case XP_EVENT -> applyXp(userId, record);
        };
    }

    private boolean applySettings(long userId, SyncRecord record, boolean isForced) {
        if (!isForced && isLocalNewer("user_settings", "user_id = ?", record, String.valueOf(userId))) {
            return true;
        }
        ContentValues values = new ContentValues();
        values.put("level", record.text("level"));
        values.put("daily_goal", record.number("daily_goal"));
        values.put("updated_at", record.updatedAt());
        values.put("dirty", 0);
        db().update("user_settings", values, "user_id = ?", new String[]{String.valueOf(userId)});
        return true;
    }

    private boolean applyDeck(long userId, SyncRecord record, boolean isForced) {
        if (!isForced && isLocalNewer("decks", "uuid = ?", record, record.key())) {
            return true;
        }
        ContentValues values = common(record);
        String name = record.isDeleted() ? record.text("name") : freeDeckName(userId, record);
        // BR-044: hai máy tạo hai bộ trùng tên thì giữ cả hai, bộ kéo về sau được đổi tên và đẩy lại.
        boolean isRenamed = !name.equals(record.text("name"));
        values.put("dirty", isRenamed ? 1 : 0);
        values.put("owner_user_id", userId);
        values.put("name", name);
        values.put("name_norm", TextNormalizer.normalize(name));
        values.put("description", record.text("description"));
        values.put("language_code", record.text("language_code"));
        values.put("icon", record.text("icon"));
        values.put("is_default", record.number("is_default"));
        values.put("sort_order", record.number("sort_order"));
        values.put("created_at", record.number("created_at"));
        values.put("deleted", record.isDeleted() ? 1 : 0);
        updateOrInsert("decks", values, record.key());
        return true;
    }

    private String freeDeckName(long userId, SyncRecord record) {
        String base = record.text("name");
        String name = base;
        for (int n = 2; isDeckNameTaken(userId, name, record.key()); n++) {
            name = base + " (" + n + ")";
        }
        return name;
    }

    private boolean isDeckNameTaken(long userId, String name, String exceptUuid) {
        return exists("SELECT 1 FROM decks WHERE owner_user_id = ? AND name_norm = ? AND deleted = 0 AND uuid <> ?",
                String.valueOf(userId), TextNormalizer.normalize(name), exceptUuid);
    }

    private boolean applyWord(SyncRecord record, boolean isForced) {
        Long deckId = id("decks", record.text("deck_uuid"));
        if (deckId == null) {
            return false;
        }
        if (!isForced && isLocalNewer("words", "uuid = ?", record, record.key())) {
            return true;
        }
        String language = string("SELECT language_code FROM decks WHERE id = ?", String.valueOf(deckId));
        String searchTerm = TextNormalizer.searchKey(record.text("term"), language);
        boolean isLoser = !record.isDeleted() && !winsDuplicate(deckId, searchTerm, record);
        ContentValues values = common(record);
        values.put("dirty", isLoser ? 1 : 0);
        values.put("deck_id", deckId);
        values.put("term", record.text("term"));
        values.put("reading", record.text("reading"));
        values.put("part_of_speech", record.text("part_of_speech"));
        values.put("meaning_vi", record.text("meaning_vi"));
        values.put("example", record.text("example"));
        values.put("example_translation", record.text("example_translation"));
        values.put("search_term", searchTerm);
        values.put("search_reading", record.has("reading") ? TextNormalizer.stripMarks(record.text("reading")) : null);
        values.put("search_meaning", TextNormalizer.stripMarks(record.text("meaning_vi")));
        values.put("sort_order", record.number("sort_order"));
        values.put("created_at", record.number("created_at"));
        values.put("deleted", record.isDeleted() || isLoser ? 1 : 0);
        updateOrInsert("words", values, record.key());
        return true;
    }

    /**
     * BR-047: hai máy thêm cùng một từ vào cùng bộ thì giữ bản sửa sau, bản kia thành tombstone và được đẩy lại.
     */
    private boolean winsDuplicate(long deckId, String searchTerm, SyncRecord record) {
        try (Cursor cursor = db().rawQuery("SELECT id, updated_at FROM words WHERE deck_id = ? AND search_term = ? "
                + "AND deleted = 0 AND uuid <> ?", new String[]{String.valueOf(deckId), searchTerm, record.key()})) {
            if (!cursor.moveToFirst()) {
                return true;
            }
            if (cursor.getLong(1) > record.updatedAt()) {
                return false;
            }
            db().execSQL("UPDATE words SET deleted = 1, dirty = 1, updated_at = ? WHERE id = ?",
                    new Object[]{record.updatedAt(), cursor.getLong(0)});
            return true;
        }
    }

    private boolean applyCard(long userId, SyncRecord record, boolean isForced) {
        Long wordId = id("words", record.key());
        if (wordId == null) {
            return false;
        }
        String[] key = {String.valueOf(userId), String.valueOf(wordId)};
        if (!isForced && isLocalNewer("card_progress", "user_id = ? AND word_id = ?", record, key)) {
            return true;
        }
        ContentValues values = new ContentValues();
        values.put("box", record.number("box"));
        values.put("next_review_date", record.text("next_review_date"));
        values.put("first_learned_date", record.text("first_learned_date"));
        values.put("last_reviewed_at", record.number("last_reviewed_at"));
        values.put("review_count", record.number("review_count"));
        putSync(values, record);
        if (db().update("card_progress", values, "user_id = ? AND word_id = ?", key) == 0) {
            values.put("user_id", userId);
            values.put("word_id", wordId);
            db().insertOrThrow("card_progress", null, values);
        }
        return true;
    }

    private boolean applyFavorite(long userId, SyncRecord record, boolean isForced) {
        Long wordId = id("words", record.key());
        if (wordId == null) {
            return false;
        }
        String[] key = {String.valueOf(userId), String.valueOf(wordId)};
        if (!isForced && isLocalNewer("favorites", "user_id = ? AND word_id = ?", record, key)) {
            return true;
        }
        ContentValues values = new ContentValues();
        values.put("created_at", record.number("created_at"));
        values.put("deleted", record.isDeleted() ? 1 : 0);
        putSync(values, record);
        if (db().update("favorites", values, "user_id = ? AND word_id = ?", key) == 0) {
            values.put("user_id", userId);
            values.put("word_id", wordId);
            db().insertOrThrow("favorites", null, values);
        }
        return true;
    }

    private boolean applyStudyLog(long userId, SyncRecord record) {
        ContentValues values = common(record);
        values.put("user_id", userId);
        values.put("activity_type", record.text("activity_type"));
        values.put("ref_uuid", record.text("ref_uuid"));
        values.put("study_date", record.text("study_date"));
        values.put("created_at", record.number("created_at"));
        insertIfAbsent("study_log", values);
        return true;
    }

    private boolean applyQuiz(long userId, SyncRecord record) {
        ContentValues values = common(record);
        values.put("user_id", userId);
        values.put("source_type", record.text("source_type"));
        values.put("deck_id", record.has("deck_ref") ? id("decks", record.text("deck_ref")) : null);
        values.put("score", record.number("score"));
        values.put("correct_count", record.number("correct_count"));
        values.put("total_count", record.number("total_count"));
        values.put("duration_sec", record.number("duration_sec"));
        values.put("created_at", record.number("created_at"));
        insertIfAbsent("quiz_attempts", values);
        return true;
    }

    private boolean applyExam(long userId, SyncRecord record) {
        String testId = string("SELECT id FROM exam_tests WHERE code = ?", record.text("test_code"));
        if (testId == null) {
            return false;
        }
        ContentValues values = common(record);
        values.put("user_id", userId);
        values.put("test_id", testId);
        values.put("status", "SUBMITTED");
        values.put("score", record.number("score"));
        values.put("correct_count", record.number("correct_count"));
        values.put("total_count", record.number("total_count"));
        values.put("duration_sec", record.number("duration_sec"));
        values.put("started_at", record.number("started_at"));
        values.put("submitted_at", record.number("submitted_at"));
        insertIfAbsent("exam_attempts", values);
        return true;
    }

    private boolean applyXp(long userId, SyncRecord record) {
        ContentValues values = common(record);
        values.put("user_id", userId);
        values.put("source_type", record.text("source_type"));
        values.put("ref_uuid", record.text("ref_uuid"));
        values.put("correct_count", record.number("correct_count"));
        values.put("wrong_count", record.number("wrong_count"));
        values.put("total_count", record.number("total_count"));
        values.put("xp_local", record.number("xp_awarded"));
        values.put("occurred_at", record.number("occurred_at"));
        values.put("local_date", Instant.ofEpochMilli(record.number("occurred_at")).atZone(VIETNAM).toLocalDate()
                .toString());
        insertIfAbsent("xp_events", values);
        if (record.has("xp_awarded")) {
            // BR-066: số XP server tính thay cho ước tính trên máy.
            ContentValues awarded = new ContentValues();
            awarded.put("xp_awarded", record.number("xp_awarded"));
            db().update("xp_events", awarded, "uuid = ?", new String[]{record.key()});
        }
        return true;
    }

    private boolean isLocalNewer(String table, String where, SyncRecord record, String... args) {
        try (Cursor cursor = db().query(table, new String[]{"updated_at"}, where + " AND dirty = 1", args, null, null,
                null)) {
            return cursor.moveToFirst() && cursor.getLong(0) > record.updatedAt();
        }
    }

    private static ContentValues common(SyncRecord record) {
        ContentValues values = new ContentValues();
        values.put("uuid", record.key());
        putSync(values, record);
        return values;
    }

    private static void putSync(ContentValues values, SyncRecord record) {
        values.put("updated_at", record.updatedAt());
        values.put("dirty", 0);
        values.put("server_seq", record.serverSeq());
    }

    private void updateOrInsert(String table, ContentValues values, String uuid) {
        if (db().update(table, values, "uuid = ?", new String[]{uuid}) == 0) {
            db().insertOrThrow(table, null, values);
        }
    }

    // Bản ghi chỉ-thêm: đã có uuid thì giữ bản trên máy.
    private void insertIfAbsent(String table, ContentValues values) {
        db().insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private Long id(String table, String uuid) {
        String found = string("SELECT id FROM " + table + " WHERE uuid = ?", uuid);
        return found == null ? null : Long.valueOf(found);
    }

    private boolean exists(String sql, String... args) {
        return string(sql, args) != null;
    }

    private String string(String sql, String... args) {
        try (Cursor cursor = db().rawQuery(sql, args)) {
            return cursor.moveToFirst() ? cursor.getString(0) : null;
        }
    }

    private SQLiteDatabase db() {
        return database.getWritableDatabase();
    }
}
