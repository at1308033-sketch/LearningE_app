package com.learninge.app.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.DbContract.ExamAnswers;
import com.learninge.app.data.db.DbContract.ExamAttempts;
import com.learninge.app.data.db.DbContract.Sync;
import com.learninge.app.data.io.OptionsJson;
import com.learninge.app.data.model.ExamAttempt;
import com.learninge.app.data.model.ExamQuestion;
import com.learninge.app.data.model.ExamTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Các bảng {@code exam_*}.
 */
public final class ExamDao {
    private static final String TEST = "SELECT t.id, t.code, t.exam_tag, t.title, t.part_label, t.question_count, "
            + "t.duration_sec, (SELECT MAX(a.score) FROM exam_attempts a WHERE a.test_id = t.id AND a.user_id = ?1 "
            + "AND a.status = 'SUBMITTED'), EXISTS (SELECT 1 FROM exam_attempts a WHERE a.test_id = t.id "
            + "AND a.user_id = ?1 AND a.status = 'IN_PROGRESS') FROM exam_tests t ";
    private static final String ATTEMPT = "SELECT a.id, a.uuid, a.test_id, a.status, a.started_at, a.submitted_at, "
            + "a.score, a.correct_count, a.total_count, a.duration_sec FROM exam_attempts a WHERE a.user_id = ?1 AND ";

    private final DatabaseHelper database;

    public ExamDao(DatabaseHelper database) {
        this.database = database;
    }

    /**
     * Đề đã ngừng dùng chỉ hiện khi còn lượt làm dở.
     */
    public List<ExamTest> listTests(long userId) {
        return tests("WHERE t.retired = 0 OR EXISTS (SELECT 1 FROM exam_attempts a WHERE a.test_id = t.id "
                + "AND a.user_id = ?1 AND a.status = 'IN_PROGRESS') ORDER BY t.sort_order, t.id", String.valueOf(userId));
    }

    public ExamTest findTest(long userId, String code) {
        List<ExamTest> found = tests("WHERE t.code = ?2", String.valueOf(userId), code);
        return found.isEmpty() ? null : found.get(0);
    }

    public ExamAttempt findInProgress(long userId, long testId) {
        return attempt("a.test_id = CAST(?2 AS INTEGER) AND a.status = 'IN_PROGRESS' ORDER BY a.id DESC LIMIT 1",
                userId, testId);
    }

    public ExamAttempt findAttempt(long userId, long attemptId) {
        return attempt("a.id = CAST(?2 AS INTEGER)", userId, attemptId);
    }

    /**
     * Các lượt đã nộp, mới nhất trước.
     */
    public List<ExamAttempt> listSubmitted(long userId) {
        return attempts("a.status = 'SUBMITTED' ORDER BY a.submitted_at DESC, a.id DESC", String.valueOf(userId));
    }

    public long insertAttempt(long userId, ExamTest test, long now) {
        ContentValues values = new ContentValues();
        values.put(Sync.UUID, UUID.randomUUID().toString());
        values.put(ExamAttempts.USER_ID, userId);
        values.put(ExamAttempts.TEST_ID, test.id());
        values.put(ExamAttempts.STATUS, "IN_PROGRESS");
        values.put(ExamAttempts.STARTED_AT, now);
        values.put(ExamAttempts.TOTAL_COUNT, test.questionCount());
        values.put(Sync.UPDATED_AT, now);
        return database.getWritableDatabase().insertOrThrow(ExamAttempts.TABLE, null, values);
    }

    /**
     * Lượt đã nộp mới được chờ đẩy.
     */
    public void submit(long attemptId, int score, int correct, int total, int durationSec, long now) {
        ContentValues values = new ContentValues();
        values.put(ExamAttempts.STATUS, "SUBMITTED");
        values.put(ExamAttempts.SUBMITTED_AT, now);
        values.put(ExamAttempts.SCORE, score);
        values.put(ExamAttempts.CORRECT_COUNT, correct);
        values.put(ExamAttempts.TOTAL_COUNT, total);
        values.put(ExamAttempts.DURATION_SEC, durationSec);
        values.put(Sync.DIRTY, 1);
        values.put(Sync.UPDATED_AT, now);
        database.getWritableDatabase().update(ExamAttempts.TABLE, values, ExamAttempts.ID + " = ?",
                new String[]{String.valueOf(attemptId)});
    }

    public List<ExamQuestion> listQuestions(ExamAttempt attempt) {
        List<ExamQuestion> questions = new ArrayList<>();
        try (Cursor cursor = database.getReadableDatabase().rawQuery("SELECT q.id, q.number, q.passage, q.question, "
                        + "q.options_json, q.correct_index, q.explanation, q.word_id, a.selected_index, "
                        + "COALESCE(a.flagged, 0) FROM exam_questions q LEFT JOIN exam_answers a "
                        + "ON a.question_id = q.id AND a.attempt_id = ? WHERE q.test_id = ? ORDER BY q.number",
                new String[]{String.valueOf(attempt.id()), String.valueOf(attempt.test().id())})) {
            while (cursor.moveToNext()) {
                questions.add(new ExamQuestion(cursor.getLong(0), cursor.getInt(1), cursor.getString(2),
                        cursor.getString(3), OptionsJson.parse(cursor.getString(4)), cursor.getInt(5),
                        cursor.getString(6), cursor.isNull(7) ? null : cursor.getLong(7),
                        cursor.isNull(8) ? null : cursor.getInt(8), cursor.getInt(9) == 1));
            }
        }
        return questions;
    }

    /**
     * {@code selectedIndex} null = chưa chọn. Khóa chính là (lượt, câu) nên ghi đè được.
     */
    public void saveAnswer(long attemptId, long questionId, Integer selectedIndex, boolean isFlagged) {
        ContentValues values = new ContentValues();
        values.put(ExamAnswers.ATTEMPT_ID, attemptId);
        values.put(ExamAnswers.QUESTION_ID, questionId);
        values.put(ExamAnswers.SELECTED_INDEX, selectedIndex);
        values.put(ExamAnswers.FLAGGED, isFlagged ? 1 : 0);
        SQLiteDatabase db = database.getWritableDatabase();
        db.insertWithOnConflict(ExamAnswers.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private ExamAttempt attempt(String where, long userId, long id) {
        List<ExamAttempt> found = attempts(where, String.valueOf(userId), String.valueOf(id));
        return found.isEmpty() ? null : found.get(0);
    }

    private List<ExamAttempt> attempts(String where, String... args) {
        List<ExamAttempt> result = new ArrayList<>();
        try (Cursor cursor = database.getReadableDatabase().rawQuery(ATTEMPT + where, args)) {
            while (cursor.moveToNext()) {
                ExamTest test = tests("WHERE t.id = CAST(?2 AS INTEGER)", args[0], cursor.getString(2)).get(0);
                result.add(new ExamAttempt(cursor.getLong(0), cursor.getString(1), test,
                        "SUBMITTED".equals(cursor.getString(3)), cursor.getLong(4), cursor.getLong(5),
                        cursor.getInt(6), cursor.getInt(7), cursor.getInt(8), cursor.getInt(9)));
            }
        }
        return result;
    }

    private List<ExamTest> tests(String where, String... args) {
        List<ExamTest> result = new ArrayList<>();
        try (Cursor cursor = database.getReadableDatabase().rawQuery(TEST + where, args)) {
            while (cursor.moveToNext()) {
                result.add(new ExamTest(cursor.getLong(0), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), cursor.getString(4), cursor.getInt(5), cursor.getInt(6),
                        cursor.isNull(7) ? null : cursor.getInt(7), cursor.getInt(8) == 1));
            }
        }
        return result;
    }
}
