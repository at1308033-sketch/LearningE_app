package com.learninge.app.data.db;

/**
 * DDL theo phiên bản. Thời điểm = epoch millis UTC, ngày = yyyy-MM-dd, boolean = 0/1.
 */
final class DbSchema {
    private static final String BOOL = " INTEGER NOT NULL DEFAULT 0 CHECK (%1$s IN (0,1))";

    private static final String SYNC = "updated_at INTEGER NOT NULL, "
            + "dirty INTEGER NOT NULL DEFAULT 1 CHECK (dirty IN (0,1)), server_seq INTEGER";
    private static final String DELETED = "deleted" + String.format(BOOL, "deleted");

    // 1 = kho máy chủ chưa từng được khôi phục.
    static final String[] V3 = {
            "ALTER TABLE sync_state ADD COLUMN data_epoch INTEGER NOT NULL DEFAULT 1"
    };

    static final String[] V2 = {
            "ALTER TABLE exam_tests ADD COLUMN retired" + String.format(BOOL, "retired")
    };

    static final String[] V1 = {
            "CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, server_uuid TEXT NOT NULL UNIQUE, "
                    + "email TEXT NOT NULL UNIQUE, nickname TEXT NOT NULL, display_name TEXT NOT NULL, "
                    + "onboarded" + String.format(BOOL, "onboarded") + ", created_at INTEGER NOT NULL)",

            "CREATE TABLE user_settings (user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE, "
                    + "level TEXT NOT NULL DEFAULT 'A1' CHECK (level IN ('A1','A2','B1')), "
                    + "daily_goal INTEGER NOT NULL DEFAULT 10 CHECK (daily_goal IN (5,10,15,20)), "
                    + "reminder_enabled INTEGER NOT NULL DEFAULT 1 CHECK (reminder_enabled IN (0,1)), "
                    + "reminder_minute INTEGER NOT NULL DEFAULT 1200 CHECK (reminder_minute BETWEEN 0 AND 1439), "
                    + "current_streak INTEGER NOT NULL DEFAULT 0 CHECK (current_streak >= 0), "
                    + "longest_streak INTEGER NOT NULL DEFAULT 0 CHECK (longest_streak >= 0), "
                    + "last_study_date TEXT, updated_at INTEGER NOT NULL, "
                    + "dirty INTEGER NOT NULL DEFAULT 0 CHECK (dirty IN (0,1)))",

            "CREATE TABLE sync_state (user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE, "
                    + "device_id TEXT NOT NULL, pull_cursor INTEGER NOT NULL DEFAULT 0 CHECK (pull_cursor >= 0), "
                    + "last_sync_at INTEGER, last_attempt_at INTEGER, last_error_code TEXT, last_error_at INTEGER)",

            "CREATE TABLE languages (code TEXT PRIMARY KEY, name_vi TEXT NOT NULL, tts_locale TEXT NOT NULL, "
                    + "reading_label TEXT NOT NULL, sort_order INTEGER NOT NULL)",

            "CREATE TABLE decks (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT NOT NULL UNIQUE, code TEXT UNIQUE, "
                    + "owner_user_id INTEGER REFERENCES users(id) ON DELETE CASCADE, name TEXT NOT NULL, "
                    + "name_norm TEXT NOT NULL, description TEXT, "
                    + "language_code TEXT NOT NULL REFERENCES languages(code), "
                    + "level TEXT CHECK (level IN ('A1','A2','B1')), icon TEXT, exam_tag TEXT, "
                    + "is_default" + String.format(BOOL, "is_default") + ", "
                    + "sort_order INTEGER NOT NULL DEFAULT 0, created_at INTEGER NOT NULL, " + DELETED + ", " + SYNC
                    // Bộ có sẵn chỉ đọc nên không bao giờ chờ đẩy (BR-043).
                    + ", CHECK (owner_user_id IS NOT NULL OR dirty = 0))",
            "CREATE INDEX idx_decks_owner ON decks(owner_user_id, sort_order)",
            "CREATE UNIQUE INDEX ux_decks_owner_name ON decks(owner_user_id, name_norm) "
                    + "WHERE deleted = 0 AND owner_user_id IS NOT NULL",
            "CREATE INDEX idx_decks_dirty ON decks(dirty) WHERE dirty = 1",

            "CREATE TABLE words (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT NOT NULL UNIQUE, "
                    + "deck_id INTEGER NOT NULL REFERENCES decks(id) ON DELETE CASCADE, term TEXT NOT NULL, "
                    + "reading TEXT, part_of_speech TEXT, meaning_vi TEXT NOT NULL, example TEXT, "
                    + "example_translation TEXT, search_term TEXT NOT NULL, search_reading TEXT, search_meaning TEXT, "
                    + "sort_order INTEGER NOT NULL DEFAULT 0, created_at INTEGER NOT NULL, " + DELETED
                    + ", " + SYNC + ")",
            "CREATE INDEX idx_words_deck ON words(deck_id, sort_order)",
            "CREATE UNIQUE INDEX ux_words_deck_term ON words(deck_id, search_term) WHERE deleted = 0",
            "CREATE INDEX idx_words_search ON words(search_term)",
            "CREATE INDEX idx_words_dirty ON words(dirty) WHERE dirty = 1",

            "CREATE TABLE card_progress (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, "
                    + "word_id INTEGER NOT NULL REFERENCES words(id) ON DELETE CASCADE, "
                    + "box INTEGER NOT NULL CHECK (box BETWEEN 1 AND 5), next_review_date TEXT NOT NULL, "
                    + "first_learned_date TEXT NOT NULL, last_reviewed_at INTEGER NOT NULL, "
                    + "review_count INTEGER NOT NULL DEFAULT 0 CHECK (review_count >= 0), " + SYNC
                    + ", UNIQUE (user_id, word_id))",
            "CREATE INDEX idx_progress_due ON card_progress(user_id, next_review_date)",
            "CREATE INDEX idx_progress_dirty ON card_progress(dirty) WHERE dirty = 1",

            "CREATE TABLE favorites (user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, "
                    + "word_id INTEGER NOT NULL REFERENCES words(id) ON DELETE CASCADE, created_at INTEGER NOT NULL, "
                    + DELETED + ", " + SYNC + ", PRIMARY KEY (user_id, word_id))",
            "CREATE INDEX idx_favorites_dirty ON favorites(dirty) WHERE dirty = 1",

            "CREATE TABLE quiz_attempts (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT NOT NULL UNIQUE, "
                    + "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, "
                    + "source_type TEXT NOT NULL CHECK (source_type IN ('DECK')), "
                    + "deck_id INTEGER REFERENCES decks(id) ON DELETE SET NULL, "
                    + "score INTEGER NOT NULL CHECK (score BETWEEN 0 AND 100), correct_count INTEGER NOT NULL, "
                    + "total_count INTEGER NOT NULL CHECK (total_count > 0), duration_sec INTEGER NOT NULL, "
                    + "created_at INTEGER NOT NULL, " + SYNC + ")",
            "CREATE INDEX idx_attempts_user_time ON quiz_attempts(user_id, created_at)",
            "CREATE INDEX idx_quiz_attempts_dirty ON quiz_attempts(dirty) WHERE dirty = 1",

            "CREATE TABLE quiz_answers (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "attempt_id INTEGER NOT NULL REFERENCES quiz_attempts(id) ON DELETE CASCADE, "
                    + "question_type TEXT NOT NULL, word_id INTEGER REFERENCES words(id) ON DELETE SET NULL, "
                    + "prompt TEXT NOT NULL, user_answer TEXT NOT NULL, correct_answer TEXT NOT NULL, "
                    + "is_correct" + String.format(BOOL, "is_correct") + ")",
            "CREATE INDEX idx_quiz_answers_attempt ON quiz_answers(attempt_id)",

            "CREATE TABLE exam_tests (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT NOT NULL UNIQUE, "
                    + "exam_tag TEXT NOT NULL, title TEXT NOT NULL, part_label TEXT, "
                    + "question_count INTEGER NOT NULL CHECK (question_count > 0), "
                    + "duration_sec INTEGER NOT NULL CHECK (duration_sec > 0), description TEXT, "
                    + "sort_order INTEGER NOT NULL DEFAULT 0)",

            "CREATE TABLE exam_questions (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "test_id INTEGER NOT NULL REFERENCES exam_tests(id) ON DELETE CASCADE, number INTEGER NOT NULL, "
                    + "passage TEXT, question TEXT NOT NULL, options_json TEXT NOT NULL, "
                    + "correct_index INTEGER NOT NULL CHECK (correct_index BETWEEN 0 AND 3), explanation TEXT, "
                    + "word_id INTEGER REFERENCES words(id) ON DELETE SET NULL, UNIQUE (test_id, number))",

            "CREATE TABLE exam_attempts (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT NOT NULL UNIQUE, "
                    + "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, "
                    + "test_id INTEGER NOT NULL REFERENCES exam_tests(id) ON DELETE CASCADE, "
                    + "status TEXT NOT NULL CHECK (status IN ('IN_PROGRESS','SUBMITTED')), "
                    + "started_at INTEGER NOT NULL, "
                    + "submitted_at INTEGER, score INTEGER CHECK (score BETWEEN 0 AND 100), correct_count INTEGER, "
                    + "total_count INTEGER NOT NULL, duration_sec INTEGER, updated_at INTEGER NOT NULL, "
                    // Chỉ lượt đã nộp mới được đẩy lên server.
                    + "dirty INTEGER NOT NULL DEFAULT 0 CHECK (dirty = 0 OR status = 'SUBMITTED'), server_seq INTEGER)",
            "CREATE INDEX idx_exam_attempts_user ON exam_attempts(user_id, test_id, status)",
            "CREATE INDEX idx_exam_attempts_dirty ON exam_attempts(dirty) WHERE dirty = 1",

            "CREATE TABLE exam_answers (attempt_id INTEGER NOT NULL REFERENCES exam_attempts(id) ON DELETE CASCADE, "
                    + "question_id INTEGER NOT NULL REFERENCES exam_questions(id) ON DELETE CASCADE, "
                    + "selected_index INTEGER CHECK (selected_index BETWEEN 0 AND 3), "
                    + "flagged" + String.format(BOOL, "flagged") + ", PRIMARY KEY (attempt_id, question_id))",

            "CREATE TABLE study_log (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT NOT NULL UNIQUE, "
                    + "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, "
                    + "activity_type TEXT NOT NULL CHECK (activity_type IN ('LEARN','REVIEW','QUIZ','EXAM')), "
                    + "ref_uuid TEXT, study_date TEXT NOT NULL, created_at INTEGER NOT NULL, " + SYNC + ")",
            "CREATE INDEX idx_log_user_date ON study_log(user_id, study_date)",
            "CREATE INDEX idx_study_log_dirty ON study_log(dirty) WHERE dirty = 1",

            "CREATE TABLE xp_events (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT NOT NULL UNIQUE, "
                    + "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, "
                    + "source_type TEXT NOT NULL CHECK (source_type IN ('CARD_SESSION','QUIZ','EXAM')), ref_uuid TEXT, "
                    + "correct_count INTEGER NOT NULL CHECK (correct_count >= 0), "
                    + "wrong_count INTEGER NOT NULL CHECK (wrong_count >= 0), "
                    + "total_count INTEGER NOT NULL CHECK (total_count = correct_count + wrong_count), "
                    + "xp_local INTEGER NOT NULL, xp_awarded INTEGER, occurred_at INTEGER NOT NULL, "
                    + "local_date TEXT NOT NULL, " + SYNC + ")",
            "CREATE INDEX idx_xp_user_date ON xp_events(user_id, local_date)",
            "CREATE INDEX idx_xp_events_dirty ON xp_events(dirty) WHERE dirty = 1",

            "CREATE TABLE leaderboard_cache (user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, "
                    + "week_start TEXT NOT NULL, payload BLOB NOT NULL, fetched_at INTEGER NOT NULL, "
                    + "PRIMARY KEY (user_id, week_start))",

            "CREATE TABLE content_version (item_key TEXT PRIMARY KEY, version INTEGER NOT NULL, "
                    + "installed_at INTEGER NOT NULL)",

            "CREATE TABLE seed_meta (key TEXT PRIMARY KEY, value TEXT NOT NULL)",
    };

    private DbSchema() {
    }
}
