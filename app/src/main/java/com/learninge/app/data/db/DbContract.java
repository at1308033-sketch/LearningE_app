package com.learninge.app.data.db;

/**
 * Tên bảng và cột của CSDL app, khai báo một lần (DATABASE-DESIGN phần A).
 */
public final class DbContract {

    private DbContract() {
    }

    /**
     * Cột đồng bộ dùng chung cho nhiều bảng.
     */
    public static final class Sync {
        public static final String UUID = "uuid";
        public static final String UPDATED_AT = "updated_at";
        public static final String DELETED = "deleted";
        public static final String DIRTY = "dirty";
        public static final String SERVER_SEQ = "server_seq";

        private Sync() {
        }
    }

    public static final class Users {
        public static final String TABLE = "users";
        public static final String ID = "id";
        public static final String SERVER_UUID = "server_uuid";
        public static final String EMAIL = "email";
        public static final String NICKNAME = "nickname";
        public static final String DISPLAY_NAME = "display_name";
        public static final String ONBOARDED = "onboarded";
        public static final String CREATED_AT = "created_at";

        private Users() {
        }
    }

    public static final class Settings {
        public static final String TABLE = "user_settings";
        public static final String USER_ID = "user_id";
        public static final String LEVEL = "level";
        public static final String DAILY_GOAL = "daily_goal";
        public static final String REMINDER_ENABLED = "reminder_enabled";
        public static final String REMINDER_MINUTE = "reminder_minute";
        public static final String CURRENT_STREAK = "current_streak";
        public static final String LONGEST_STREAK = "longest_streak";
        public static final String LAST_STUDY_DATE = "last_study_date";

        private Settings() {
        }
    }

    public static final class SyncState {
        public static final String TABLE = "sync_state";
        public static final String USER_ID = "user_id";
        public static final String DEVICE_ID = "device_id";
        public static final String PULL_CURSOR = "pull_cursor";
        public static final String LAST_SYNC_AT = "last_sync_at";
        public static final String LAST_ATTEMPT_AT = "last_attempt_at";
        public static final String LAST_ERROR_CODE = "last_error_code";
        public static final String LAST_ERROR_AT = "last_error_at";

        private SyncState() {
        }
    }

    public static final class Languages {
        public static final String TABLE = "languages";
        public static final String CODE = "code";
        public static final String NAME_VI = "name_vi";
        public static final String TTS_LOCALE = "tts_locale";
        public static final String READING_LABEL = "reading_label";
        public static final String SORT_ORDER = "sort_order";

        private Languages() {
        }
    }

    public static final class Decks {
        public static final String TABLE = "decks";
        public static final String ID = "id";
        public static final String CODE = "code";
        public static final String OWNER_USER_ID = "owner_user_id";
        public static final String NAME = "name";
        public static final String NAME_NORM = "name_norm";
        public static final String DESCRIPTION = "description";
        public static final String LANGUAGE_CODE = "language_code";
        public static final String LEVEL = "level";
        public static final String ICON = "icon";
        public static final String EXAM_TAG = "exam_tag";
        public static final String IS_DEFAULT = "is_default";
        public static final String SORT_ORDER = "sort_order";
        public static final String CREATED_AT = "created_at";

        private Decks() {
        }
    }

    public static final class Words {
        public static final String TABLE = "words";
        public static final String ID = "id";
        public static final String DECK_ID = "deck_id";
        public static final String TERM = "term";
        public static final String READING = "reading";
        public static final String PART_OF_SPEECH = "part_of_speech";
        public static final String MEANING_VI = "meaning_vi";
        public static final String EXAMPLE = "example";
        public static final String EXAMPLE_TRANSLATION = "example_translation";
        public static final String SEARCH_TERM = "search_term";
        public static final String SEARCH_READING = "search_reading";
        public static final String SEARCH_MEANING = "search_meaning";
        public static final String SORT_ORDER = "sort_order";
        public static final String CREATED_AT = "created_at";

        private Words() {
        }
    }

    public static final class CardProgress {
        public static final String TABLE = "card_progress";
        public static final String ID = "id";
        public static final String USER_ID = "user_id";
        public static final String WORD_ID = "word_id";
        public static final String BOX = "box";
        public static final String NEXT_REVIEW_DATE = "next_review_date";
        public static final String FIRST_LEARNED_DATE = "first_learned_date";
        public static final String LAST_REVIEWED_AT = "last_reviewed_at";
        public static final String REVIEW_COUNT = "review_count";

        private CardProgress() {
        }
    }

    public static final class Favorites {
        public static final String TABLE = "favorites";
        public static final String USER_ID = "user_id";
        public static final String WORD_ID = "word_id";
        public static final String CREATED_AT = "created_at";

        private Favorites() {
        }
    }

    public static final class QuizAttempts {
        public static final String TABLE = "quiz_attempts";
        public static final String ID = "id";
        public static final String USER_ID = "user_id";
        public static final String SOURCE_TYPE = "source_type";
        public static final String DECK_ID = "deck_id";
        public static final String SCORE = "score";
        public static final String CORRECT_COUNT = "correct_count";
        public static final String TOTAL_COUNT = "total_count";
        public static final String DURATION_SEC = "duration_sec";
        public static final String CREATED_AT = "created_at";

        private QuizAttempts() {
        }
    }

    public static final class QuizAnswers {
        public static final String TABLE = "quiz_answers";

        private QuizAnswers() {
        }
    }

    public static final class ExamTests {
        public static final String TABLE = "exam_tests";
        public static final String ID = "id";
        public static final String CODE = "code";
        public static final String EXAM_TAG = "exam_tag";
        public static final String TITLE = "title";
        public static final String PART_LABEL = "part_label";
        public static final String QUESTION_COUNT = "question_count";
        public static final String DURATION_SEC = "duration_sec";
        public static final String DESCRIPTION = "description";
        public static final String SORT_ORDER = "sort_order";

        private ExamTests() {
        }
    }

    public static final class ExamQuestions {
        public static final String TABLE = "exam_questions";
        public static final String ID = "id";
        public static final String TEST_ID = "test_id";
        public static final String NUMBER = "number";
        public static final String PASSAGE = "passage";
        public static final String QUESTION = "question";
        public static final String OPTIONS_JSON = "options_json";
        public static final String CORRECT_INDEX = "correct_index";
        public static final String EXPLANATION = "explanation";
        public static final String WORD_ID = "word_id";

        private ExamQuestions() {
        }
    }

    public static final class ExamAttempts {
        public static final String TABLE = "exam_attempts";
        public static final String ID = "id";
        public static final String USER_ID = "user_id";
        public static final String TEST_ID = "test_id";
        public static final String STATUS = "status";
        public static final String STARTED_AT = "started_at";
        public static final String SUBMITTED_AT = "submitted_at";
        public static final String SCORE = "score";
        public static final String CORRECT_COUNT = "correct_count";
        public static final String TOTAL_COUNT = "total_count";
        public static final String DURATION_SEC = "duration_sec";

        private ExamAttempts() {
        }
    }

    public static final class ExamAnswers {
        public static final String TABLE = "exam_answers";
        public static final String ATTEMPT_ID = "attempt_id";
        public static final String QUESTION_ID = "question_id";
        public static final String SELECTED_INDEX = "selected_index";
        public static final String FLAGGED = "flagged";

        private ExamAnswers() {
        }
    }

    public static final class StudyLog {
        public static final String TABLE = "study_log";
        public static final String USER_ID = "user_id";
        public static final String ACTIVITY_TYPE = "activity_type";
        public static final String REF_UUID = "ref_uuid";
        public static final String STUDY_DATE = "study_date";
        public static final String CREATED_AT = "created_at";

        private StudyLog() {
        }
    }

    public static final class XpEvents {
        public static final String TABLE = "xp_events";
        public static final String USER_ID = "user_id";
        public static final String SOURCE_TYPE = "source_type";
        public static final String REF_UUID = "ref_uuid";
        public static final String CORRECT_COUNT = "correct_count";
        public static final String WRONG_COUNT = "wrong_count";
        public static final String TOTAL_COUNT = "total_count";
        public static final String XP_LOCAL = "xp_local";
        public static final String XP_AWARDED = "xp_awarded";
        public static final String OCCURRED_AT = "occurred_at";
        public static final String LOCAL_DATE = "local_date";

        private XpEvents() {
        }
    }

    public static final class LeaderboardCache {
        public static final String TABLE = "leaderboard_cache";
        public static final String USER_ID = "user_id";
        public static final String WEEK_START = "week_start";
        public static final String PAYLOAD = "payload";
        public static final String FETCHED_AT = "fetched_at";

        private LeaderboardCache() {
        }
    }

    public static final class ContentVersion {
        public static final String TABLE = "content_version";
        public static final String ITEM_KEY = "item_key";
        public static final String VERSION = "version";
        public static final String INSTALLED_AT = "installed_at";

        private ContentVersion() {
        }
    }

    public static final class SeedMeta {
        public static final String TABLE = "seed_meta";
        public static final String KEY = "key";
        public static final String VALUE = "value";

        private SeedMeta() {
        }
    }
}
