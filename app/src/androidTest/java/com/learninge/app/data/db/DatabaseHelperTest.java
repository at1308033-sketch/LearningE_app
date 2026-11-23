package com.learninge.app.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DatabaseHelperTest {
    private DatabaseHelper helper;
    private SQLiteDatabase db;

    @Before
    public void open() {
        helper = new DatabaseHelper(InstrumentationRegistry.getInstrumentation().getTargetContext(), null);
        db = helper.getWritableDatabase();
        db.execSQL("INSERT INTO users (id, server_uuid, email, nickname, display_name, created_at) "
                + "VALUES (1, 'u-1', 'a@learninge.test', 'minh', 'Minh', 0)");
        db.execSQL("INSERT INTO languages VALUES ('en', 'Tiếng Anh', 'en-US', 'Phiên âm (IPA)', 1)");
        deck(10, "d-10", "1", "so tay", 0);
        word(100, "w-100", 10, "apple", 0);
    }

    @After
    public void close() {
        helper.close();
    }

    @Test
    public void words_sameTermInDeck_rejectedUnlessDeleted() {
        assertThrows(SQLiteConstraintException.class, () -> word(101, "w-101", 10, "apple", 0));
        word(102, "w-102", 10, "apple", 1);
        assertEquals(2, count("words"));
    }

    @Test
    public void decks_sameNameForOwner_rejectedUnlessDeleted() {
        assertThrows(SQLiteConstraintException.class, () -> deck(11, "d-11", "1", "so tay", 0));
        deck(12, "d-12", "1", "so tay", 1);
        assertEquals(2, count("decks"));
    }

    @Test
    public void builtinDeck_cannotBeDirty() {
        assertThrows(SQLiteConstraintException.class, () -> deck(13, "T01", "NULL", "gia dinh", 0));
    }

    @Test
    public void deleteUser_cascadesToAllOwnedData() {
        db.execSQL("INSERT INTO user_settings (user_id, updated_at) VALUES (1, 0)");
        db.execSQL("INSERT INTO card_progress (user_id, word_id, box, next_review_date, first_learned_date, "
                + "last_reviewed_at, updated_at) VALUES (1, 100, 2, '2026-11-12', '2026-11-10', 0, 0)");
        db.execSQL("INSERT INTO favorites (user_id, word_id, created_at, updated_at) VALUES (1, 100, 0, 0)");
        db.delete("users", "id = ?", new String[]{"1"});
        for (String table : new String[]{"user_settings", "decks", "words", "card_progress", "favorites"}) {
            assertEquals(table, 0, count(table));
        }
    }

    @Test
    public void xpEvents_totalMustEqualSum() {
        assertThrows(SQLiteConstraintException.class, () -> db.execSQL("INSERT INTO xp_events (uuid, user_id, "
                + "source_type, correct_count, wrong_count, total_count, xp_local, occurred_at, local_date, updated_at) "
                + "VALUES ('x', 1, 'QUIZ', 3, 2, 6, 3, 0, '2026-11-10', 0)"));
    }

    @Test
    public void examAttempt_onlySubmittedCanBeDirty() {
        db.execSQL("INSERT INTO exam_tests (id, code, exam_tag, title, question_count, duration_sec) "
                + "VALUES (1, 'E01', 'TOEIC', 'Đề 1', 20, 900)");
        assertThrows(SQLiteConstraintException.class, () -> db.execSQL("INSERT INTO exam_attempts (uuid, user_id, "
                + "test_id, status, started_at, total_count, updated_at, dirty) "
                + "VALUES ('e', 1, 1, 'IN_PROGRESS', 0, 20, 0, 1)"));
    }

    @Test
    public void searchQuery_usesIndex() {
        try (Cursor plan = db.rawQuery("EXPLAIN QUERY PLAN SELECT id FROM words WHERE search_term >= ? AND "
                + "search_term < ?", new String[]{"ap", "aq"})) {
            assertTrue(plan.moveToFirst());
            assertTrue(plan.getString(3).contains("idx_words_search"));
        }
    }

    private void deck(int id, String uuid, String owner, String nameNorm, int deleted) {
        db.execSQL("INSERT INTO decks (id, uuid, owner_user_id, name, name_norm, language_code, created_at, "
                + "updated_at, deleted) VALUES (" + id + ", '" + uuid + "', " + owner + ", 'x', '" + nameNorm
                + "', 'en', 0, 0, " + deleted + ")");
    }

    private void word(int id, String uuid, int deckId, String term, int deleted) {
        db.execSQL("INSERT INTO words (id, uuid, deck_id, term, meaning_vi, search_term, created_at, updated_at, "
                + "deleted) VALUES (" + id + ", '" + uuid + "', " + deckId + ", '" + term + "', 'x', '" + term
                + "', 0, 0, " + deleted + ")");
    }

    private int count(String table) {
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + table, null)) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
    }
}
