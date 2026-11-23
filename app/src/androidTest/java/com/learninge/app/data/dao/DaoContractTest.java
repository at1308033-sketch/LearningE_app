package com.learninge.app.data.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.database.Cursor;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.learninge.app.TestApp;
import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.model.XpSource;
import com.learninge.app.data.repo.AccountRepository;
import com.learninge.app.domain.rules.StreakCalculator;
import com.learninge.app.domain.rules.WordInput;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Hợp đồng chung của các DAO trên SQLite thật (T-080): cờ đồng bộ, không trùng dòng, xóa liên đới, dùng index.
 */
@RunWith(AndroidJUnit4.class)
public class DaoContractTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 10);
    private static final List<String> USER_TABLES = List.of("user_settings", "sync_state", "decks", "card_progress",
            "favorites", "study_log", "quiz_attempts", "exam_attempts", "xp_events", "leaderboard_cache");

    private TestApp app;
    private DatabaseHelper database;

    @Before
    public void open() {
        app = new TestApp();
        database = app.services.database();
        app.deck(1, "T01", null, "A1");
        app.word(11, 1, "mother");
    }

    @After
    public void close() {
        app.close();
    }

    @Test
    public void cardProgress_savedTwice_isOneDirtyRowWithNewTime() {
        CardProgressDao cards = new CardProgressDao(database);
        cards.save(1, 11, 2, TODAY.plusDays(2), TODAY, 100);
        app.sql("UPDATE card_progress SET dirty = 0");
        cards.save(1, 11, 3, TODAY.plusDays(4), TODAY, 200);
        assertEquals(1, app.number("SELECT COUNT(*) FROM card_progress"));
        assertEquals(1, app.number("SELECT dirty FROM card_progress"));
        assertEquals(200, app.number("SELECT updated_at FROM card_progress"));
    }

    @Test
    public void deckAndWord_everyWriteSetsDirtyAndUpdatedAt() {
        DeckDao decks = new DeckDao(database);
        WordDao words = new WordDao(database);
        long deckId = decks.insert(1, "Bộ", null, "en", 100);
        long wordId = words.insert(deckId, word("cat"), "en", 100);
        app.sql("UPDATE decks SET dirty = 0 WHERE id = " + deckId);
        app.sql("UPDATE words SET dirty = 0 WHERE id = " + wordId);
        decks.update(deckId, "Bộ mới", null, "en", 200);
        words.update(wordId, word("dog"), "en", 200);
        assertEquals(200, app.number("SELECT updated_at FROM decks WHERE dirty = 1 AND id = " + deckId));
        assertEquals(200, app.number("SELECT updated_at FROM words WHERE dirty = 1 AND id = " + wordId));
        app.sql("UPDATE words SET dirty = 0 WHERE id = " + wordId);
        words.markDeleted(wordId, 300);
        assertEquals(300, app.number("SELECT updated_at FROM words WHERE dirty = 1 AND deleted = 1 AND id = "
                + wordId));
    }

    @Test
    public void favorite_toggledManyTimes_isOneRow() {
        FavoriteDao favorites = new FavoriteDao(database);
        for (int i = 0; i < 5; i++) {
            favorites.set(1, 11, i % 2 == 0, i);
        }
        assertTrue(favorites.isFavorite(1, 11));
        assertEquals(1, app.number("SELECT COUNT(*) FROM favorites"));
    }

    @Test
    public void streak_isNotMarkedForSync() {
        new SettingsDao(database).saveStreak(1, StreakCalculator.of(List.of(TODAY)));
        assertEquals(1, app.number("SELECT current_streak FROM user_settings WHERE user_id = 1"));
        assertEquals(0, app.number("SELECT dirty FROM user_settings WHERE user_id = 1"));
    }

    @Test
    public void leaderboardCache_keepsTwoWeeks() {
        LeaderboardCacheDao cache = new LeaderboardCacheDao(database);
        for (int week = 0; week < 4; week++) {
            cache.save(1, LocalDate.of(2026, 11, 2).plusWeeks(week), new byte[]{(byte) week}, week);
        }
        assertEquals(2, app.number("SELECT COUNT(*) FROM leaderboard_cache"));
        assertEquals(3, cache.findLatest(1).payload()[0]);
    }

    @Test
    public void deleteAccount_removesEveryRowOfUserAndNothingElse() {
        app.user(2);
        fillEverything();
        for (String table : USER_TABLES) {
            assertTrue(table + " phải có dữ liệu trước khi xóa", countOfUser1(table) > 0);
        }
        new AccountRepository(database).delete(1);
        for (String table : USER_TABLES) {
            assertEquals(table, 0, countOfUser1(table));
        }
        assertEquals(0, app.number("SELECT COUNT(*) FROM quiz_answers"));
        assertNull(new UserDao(database).findById(1));
        assertEquals(1, app.number("SELECT COUNT(*) FROM users"));
        // Nội dung có sẵn không thuộc người dùng nào nên còn nguyên.
        assertEquals(1, app.number("SELECT COUNT(*) FROM words"));
    }

    @Test
    public void hotQueries_useIndexes() {
        assertPlan("idx_progress_due", "SELECT id FROM card_progress WHERE user_id = 1 AND next_review_date <= 'x'");
        assertPlan("idx_words_deck", "SELECT id FROM words WHERE deck_id = 1 ORDER BY sort_order");
        assertPlan("idx_log_user_date", "SELECT id FROM study_log WHERE user_id = 1 AND study_date >= 'x'");
        assertPlan("idx_xp_user_date", "SELECT id FROM xp_events WHERE user_id = 1 AND local_date >= 'x'");
        assertPlan("idx_words_search", "SELECT id FROM words WHERE search_term >= 'a' AND search_term < 'b'");
    }

    private int countOfUser1(String table) {
        String owner = table.equals("decks") ? "owner_user_id" : "user_id";
        return app.number("SELECT COUNT(*) FROM " + table + " WHERE " + owner + " = 1");
    }

    private void fillEverything() {
        new CardProgressDao(database).save(1, 11, 2, TODAY, TODAY, 1);
        new FavoriteDao(database).set(1, 11, true, 1);
        long deckId = new DeckDao(database).insert(1, "Bộ", null, "en", 1);
        new WordDao(database).insert(deckId, word("cat"), "en", 1);
        new XpDao(database).insert(1, XpSource.QUIZ, null, 1, 0, 1, TODAY, 1);
        new LeaderboardCacheDao(database).save(1, TODAY, new byte[]{1}, 1);
        app.sql("INSERT INTO study_log (uuid, user_id, activity_type, study_date, created_at, updated_at) "
                + "VALUES ('s', 1, 'QUIZ', '2026-11-10', 0, 0)");
        app.sql("INSERT INTO quiz_attempts (id, uuid, user_id, source_type, deck_id, score, correct_count, "
                + "total_count, duration_sec, created_at, updated_at) VALUES (1, 'q', 1, 'DECK', 1, 1, 1, 1, 1, 0, 0)");
        app.sql("INSERT INTO quiz_answers (attempt_id, question_type, prompt, user_answer, correct_answer) "
                + "VALUES (1, 'x', 'x', 'x', 'x')");
        app.sql("INSERT INTO exam_tests (id, code, exam_tag, title, question_count, duration_sec) "
                + "VALUES (1, 'E01', 'TOEIC', 'Đề', 1, 60)");
        app.sql("INSERT INTO exam_attempts (uuid, user_id, test_id, status, started_at, total_count, updated_at) "
                + "VALUES ('e', 1, 1, 'IN_PROGRESS', 0, 1, 0)");
    }

    private void assertPlan(String index, String query) {
        List<String> steps = new ArrayList<>();
        try (Cursor cursor = app.db.rawQuery("EXPLAIN QUERY PLAN " + query, null)) {
            while (cursor.moveToNext()) {
                steps.add(cursor.getString(3));
            }
        }
        assertTrue(query + " → " + steps, steps.toString().contains(index));
    }

    private static WordInput word(String term) {
        return new WordInput(term, null, "nghĩa", null, null);
    }
}
