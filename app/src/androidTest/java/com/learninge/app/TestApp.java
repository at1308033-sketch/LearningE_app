package com.learninge.app;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.platform.app.InstrumentationRegistry;

import com.learninge.app.core.ServiceLocator;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Bộ Service thật trên CSDL trong bộ nhớ, đã đăng nhập người dùng 1, giờ cố định 10:00 ngày 2026-11-10.
 */
public final class TestApp {
    public static final String TODAY = "2026-11-10";

    public final ServiceLocator services;
    public final SQLiteDatabase db;

    public TestApp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        Clock clock = Clock.fixed(Instant.parse("2026-11-10T03:00:00Z"), zone);
        services = ServiceLocator.inMemory(context, clock);
        db = services.database().getWritableDatabase();
        sql("INSERT INTO languages VALUES ('en', 'Tiếng Anh', 'en-US', 'Phiên âm (IPA)', 1)");
        sql("INSERT INTO languages VALUES ('ja', 'Tiếng Nhật', 'ja-JP', 'Cách đọc (kana/romaji)', 2)");
        user(1);
        services.sessionStore().signIn(1);
    }

    public void close() {
        services.sessionStore().signOut();
        services.database().close();
    }

    public void user(long id) {
        sql("INSERT INTO users (id, server_uuid, email, nickname, display_name, created_at) VALUES (" + id + ", 'u-"
                + id + "', 'u" + id + "@learninge.test', 'nick" + id + "', 'Minh', 0)");
        sql("INSERT INTO user_settings (user_id, updated_at) VALUES (" + id + ", 0)");
        sql("INSERT INTO sync_state (user_id, device_id) VALUES (" + id + ", 'device')");
    }

    /**
     * {@code owner} null = bộ có sẵn (uuid = mã bộ).
     */
    public void deck(long id, String uuid, Long owner, String level) {
        sql("INSERT INTO decks (id, uuid, code, owner_user_id, name, name_norm, language_code, level, sort_order, "
                + "created_at, updated_at, dirty) VALUES (" + id + ", '" + uuid + "', "
                + (owner == null ? "'" + uuid + "'" : "NULL") + ", " + owner + ", 'Bộ " + uuid + "', 'bộ " + uuid
                + "', 'en', " + (level == null ? "NULL" : "'" + level + "'") + ", " + id + ", 0, 0, 0)");
    }

    public void word(long id, long deckId, String term) {
        sql("INSERT INTO words (id, uuid, deck_id, term, reading, meaning_vi, search_term, sort_order, created_at, "
                + "updated_at, dirty) VALUES (" + id + ", 'w-" + id + "', " + deckId + ", '" + term + "', '/x/', 'nghĩa "
                + term + "', '" + term + "', " + id + ", 0, 0, 0)");
    }

    public void progress(long wordId, int box, String nextReview) {
        sql("INSERT INTO card_progress (user_id, word_id, box, next_review_date, first_learned_date, "
                + "last_reviewed_at, updated_at, dirty) VALUES (1, " + wordId + ", " + box + ", '" + nextReview
                + "', '2026-11-01', 0, 0, 0)");
    }

    public void sql(String statement) {
        db.execSQL(statement);
    }

    public int number(String query) {
        try (Cursor cursor = db.rawQuery(query, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : -1;
        }
    }

    public String text(String query) {
        try (Cursor cursor = db.rawQuery(query, null)) {
            return cursor.moveToFirst() ? cursor.getString(0) : null;
        }
    }
}
