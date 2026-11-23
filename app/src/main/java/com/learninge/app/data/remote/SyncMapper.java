package com.learninge.app.data.remote;

import com.learninge.app.data.model.SyncRecord;
import com.learninge.app.data.model.SyncType;
import com.learninge.proto.app.v1.ActivityType;
import com.learninge.proto.app.v1.CardProgressData;
import com.learninge.proto.app.v1.CefrLevel;
import com.learninge.proto.app.v1.Change;
import com.learninge.proto.app.v1.DeckData;
import com.learninge.proto.app.v1.ExamAttemptData;
import com.learninge.proto.app.v1.FavoriteData;
import com.learninge.proto.app.v1.QuizAttemptData;
import com.learninge.proto.app.v1.QuizSourceType;
import com.learninge.proto.app.v1.SettingsData;
import com.learninge.proto.app.v1.StudyLogData;
import com.learninge.proto.app.v1.WordData;
import com.learninge.proto.app.v1.XpEventData;
import com.learninge.proto.app.v1.XpSourceType;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link SyncRecord} ↔ message {@code Change}. Chỗ duy nhất biết tên trường proto của đồng bộ.
 */
final class SyncMapper {

    private SyncMapper() {
    }

    static Change toProto(SyncRecord record) {
        Change.Builder change = Change.newBuilder()
                .setRecordId(record.key())
                .setUpdatedAtMs(record.updatedAt())
                .setDeleted(record.isDeleted());
        switch (record.type()) {
            case SETTINGS -> change.setSettings(SettingsData.newBuilder()
                    .setLevel(CefrLevel.valueOf("CEFR_LEVEL_" + record.text("level")))
                    .setDailyGoal((int) record.number("daily_goal")));
            case DECK -> change.setDeck(deck(record));
            case WORD -> change.setWord(word(record));
            case CARD_PROGRESS -> change.setCardProgress(CardProgressData.newBuilder()
                    .setBox((int) record.number("box"))
                    .setNextReviewDate(record.text("next_review_date"))
                    .setFirstLearnedDate(record.text("first_learned_date"))
                    .setLastReviewedAtMs(record.number("last_reviewed_at"))
                    .setReviewCount((int) record.number("review_count")));
            case FAVORITE -> change.setFavorite(FavoriteData.newBuilder().setCreatedAtMs(record.number("created_at")));
            case STUDY_LOG -> change.setStudyLog(studyLog(record));
            case QUIZ_ATTEMPT -> change.setQuizAttempt(quiz(record));
            case EXAM_ATTEMPT -> change.setExamAttempt(ExamAttemptData.newBuilder()
                    .setTestCode(record.text("test_code"))
                    .setScore((int) record.number("score"))
                    .setCorrectCount((int) record.number("correct_count"))
                    .setTotalCount((int) record.number("total_count"))
                    .setDurationSec((int) record.number("duration_sec"))
                    .setStartedAtMs(record.number("started_at"))
                    .setSubmittedAtMs(record.number("submitted_at")));
            case XP_EVENT -> change.setXpEvent(xp(record));
            default -> throw new IllegalArgumentException(record.type().name());
        }
        return change.build();
    }

    /**
     * null nếu server gửi loại bản ghi app này chưa biết (bản app cũ).
     */
    static SyncRecord fromProto(Change change) {
        Map<String, Object> fields = new HashMap<>();
        SyncType type;
        switch (change.getDataCase()) {
            case SETTINGS -> {
                type = SyncType.SETTINGS;
                fields.put("level", suffix(change.getSettings().getLevel().name(), "CEFR_LEVEL_"));
                fields.put("daily_goal", change.getSettings().getDailyGoal());
            }
            case DECK -> {
                type = SyncType.DECK;
                readDeck(change.getDeck(), fields);
            }
            case WORD -> {
                type = SyncType.WORD;
                readWord(change.getWord(), fields);
            }
            case CARD_PROGRESS -> {
                type = SyncType.CARD_PROGRESS;
                readCard(change.getCardProgress(), fields);
            }
            case FAVORITE -> {
                type = SyncType.FAVORITE;
                fields.put("created_at", change.getFavorite().getCreatedAtMs());
            }
            case STUDY_LOG -> {
                type = SyncType.STUDY_LOG;
                readStudyLog(change.getStudyLog(), fields);
            }
            case QUIZ_ATTEMPT -> {
                type = SyncType.QUIZ_ATTEMPT;
                readQuiz(change.getQuizAttempt(), fields);
            }
            case EXAM_ATTEMPT -> {
                type = SyncType.EXAM_ATTEMPT;
                readExam(change.getExamAttempt(), fields);
            }
            case XP_EVENT -> {
                type = SyncType.XP_EVENT;
                readXp(change.getXpEvent(), fields);
            }
            default -> {
                return null;
            }
        }
        return new SyncRecord(type, change.getRecordId(), change.getUpdatedAtMs(), change.getDeleted(),
                change.getServerSeq(), fields);
    }

    private static DeckData deck(SyncRecord record) {
        DeckData.Builder data = DeckData.newBuilder()
                .setName(record.text("name"))
                .setLanguageCode(record.text("language_code"))
                .setIsDefault(record.number("is_default") == 1)
                .setSortOrder((int) record.number("sort_order"))
                .setCreatedAtMs(record.number("created_at"));
        if (record.has("description")) {
            data.setDescription(record.text("description"));
        }
        if (record.has("icon")) {
            data.setIcon(record.text("icon"));
        }
        return data.build();
    }

    private static WordData word(SyncRecord record) {
        WordData.Builder data = WordData.newBuilder()
                .setDeckUuid(record.text("deck_uuid"))
                .setTerm(record.text("term"))
                .setMeaningVi(record.text("meaning_vi"))
                .setSortOrder((int) record.number("sort_order"))
                .setCreatedAtMs(record.number("created_at"));
        if (record.has("reading")) {
            data.setReading(record.text("reading"));
        }
        if (record.has("part_of_speech")) {
            data.setPartOfSpeech(record.text("part_of_speech"));
        }
        if (record.has("example")) {
            data.setExample(record.text("example"));
        }
        if (record.has("example_translation")) {
            data.setExampleTranslation(record.text("example_translation"));
        }
        return data.build();
    }

    private static StudyLogData studyLog(SyncRecord record) {
        StudyLogData.Builder data = StudyLogData.newBuilder()
                .setActivityType(ActivityType.valueOf("ACTIVITY_TYPE_" + record.text("activity_type")))
                .setStudyDate(record.text("study_date"))
                .setCreatedAtMs(record.number("created_at"));
        if (record.has("ref_uuid")) {
            data.setRefUuid(record.text("ref_uuid"));
        }
        return data.build();
    }

    private static QuizAttemptData quiz(SyncRecord record) {
        QuizAttemptData.Builder data = QuizAttemptData.newBuilder()
                .setSourceType(QuizSourceType.valueOf("QUIZ_SOURCE_TYPE_" + record.text("source_type")))
                .setScore((int) record.number("score"))
                .setCorrectCount((int) record.number("correct_count"))
                .setTotalCount((int) record.number("total_count"))
                .setDurationSec((int) record.number("duration_sec"))
                .setCreatedAtMs(record.number("created_at"));
        if (record.has("deck_ref")) {
            data.setDeckRef(record.text("deck_ref"));
        }
        return data.build();
    }

    private static XpEventData xp(SyncRecord record) {
        XpEventData.Builder data = XpEventData.newBuilder()
                .setSourceType(XpSourceType.valueOf("XP_SOURCE_TYPE_" + record.text("source_type")))
                .setCorrectCount((int) record.number("correct_count"))
                .setWrongCount((int) record.number("wrong_count"))
                .setTotalCount((int) record.number("total_count"))
                .setOccurredAtMs(record.number("occurred_at"));
        if (record.has("ref_uuid")) {
            data.setRefUuid(record.text("ref_uuid"));
        }
        return data.build();
    }

    private static void readDeck(DeckData data, Map<String, Object> fields) {
        fields.put("name", data.getName());
        fields.put("description", data.hasDescription() ? data.getDescription() : null);
        fields.put("language_code", data.getLanguageCode());
        fields.put("icon", data.hasIcon() ? data.getIcon() : null);
        fields.put("is_default", data.getIsDefault() ? 1 : 0);
        fields.put("sort_order", data.getSortOrder());
        fields.put("created_at", data.getCreatedAtMs());
    }

    private static void readWord(WordData data, Map<String, Object> fields) {
        fields.put("deck_uuid", data.getDeckUuid());
        fields.put("term", data.getTerm());
        fields.put("reading", data.hasReading() ? data.getReading() : null);
        fields.put("part_of_speech", data.hasPartOfSpeech() ? data.getPartOfSpeech() : null);
        fields.put("meaning_vi", data.getMeaningVi());
        fields.put("example", data.hasExample() ? data.getExample() : null);
        fields.put("example_translation", data.hasExampleTranslation() ? data.getExampleTranslation() : null);
        fields.put("sort_order", data.getSortOrder());
        fields.put("created_at", data.getCreatedAtMs());
    }

    private static void readCard(CardProgressData data, Map<String, Object> fields) {
        fields.put("box", data.getBox());
        fields.put("next_review_date", data.getNextReviewDate());
        fields.put("first_learned_date", data.getFirstLearnedDate());
        fields.put("last_reviewed_at", data.getLastReviewedAtMs());
        fields.put("review_count", data.getReviewCount());
    }

    private static void readStudyLog(StudyLogData data, Map<String, Object> fields) {
        fields.put("activity_type", suffix(data.getActivityType().name(), "ACTIVITY_TYPE_"));
        fields.put("ref_uuid", data.hasRefUuid() ? data.getRefUuid() : null);
        fields.put("study_date", data.getStudyDate());
        fields.put("created_at", data.getCreatedAtMs());
    }

    private static void readQuiz(QuizAttemptData data, Map<String, Object> fields) {
        fields.put("source_type", suffix(data.getSourceType().name(), "QUIZ_SOURCE_TYPE_"));
        fields.put("deck_ref", data.hasDeckRef() ? data.getDeckRef() : null);
        fields.put("score", data.getScore());
        fields.put("correct_count", data.getCorrectCount());
        fields.put("total_count", data.getTotalCount());
        fields.put("duration_sec", data.getDurationSec());
        fields.put("created_at", data.getCreatedAtMs());
    }

    private static void readExam(ExamAttemptData data, Map<String, Object> fields) {
        fields.put("test_code", data.getTestCode());
        fields.put("score", data.getScore());
        fields.put("correct_count", data.getCorrectCount());
        fields.put("total_count", data.getTotalCount());
        fields.put("duration_sec", data.getDurationSec());
        fields.put("started_at", data.getStartedAtMs());
        fields.put("submitted_at", data.getSubmittedAtMs());
    }

    private static void readXp(XpEventData data, Map<String, Object> fields) {
        fields.put("source_type", suffix(data.getSourceType().name(), "XP_SOURCE_TYPE_"));
        fields.put("ref_uuid", data.hasRefUuid() ? data.getRefUuid() : null);
        fields.put("correct_count", data.getCorrectCount());
        fields.put("wrong_count", data.getWrongCount());
        fields.put("total_count", data.getTotalCount());
        fields.put("occurred_at", data.getOccurredAtMs());
        fields.put("xp_awarded", data.hasXpAwarded() ? data.getXpAwarded() : null);
    }

    private static String suffix(String name, String prefix) {
        return name.startsWith(prefix) ? name.substring(prefix.length()) : name;
    }
}
