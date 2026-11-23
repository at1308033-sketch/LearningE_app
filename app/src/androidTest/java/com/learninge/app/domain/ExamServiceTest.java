package com.learninge.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.learninge.app.TestApp;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.ExamSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ExamServiceTest {
    private TestApp app;
    private ExamService service;

    @Before
    public void open() {
        app = new TestApp();
        service = app.services.examService();
        app.deck(1, "X01", null, "B1");
        app.word(11, 1, "submit");
        app.sql("INSERT INTO exam_tests (id, code, exam_tag, title, question_count, duration_sec) "
                + "VALUES (1, 'E01', 'TOEIC', 'Đề 1', 3, 900)");
        for (int i = 1; i <= 3; i++) {
            app.sql("INSERT INTO exam_questions (test_id, number, question, options_json, correct_index, word_id) "
                    + "VALUES (1, " + i + ", 'câu " + i + "', '[\"a\",\"b\",\"c\",\"d\"]', 1, "
                    + (i == 3 ? "11" : "NULL") + ")");
        }
    }

    @After
    public void close() {
        app.close();
    }

    @Test
    public void startOrResume_twice_keepsSameAttemptAndAnswers() {
        ExamSession first = service.startOrResume("E01").value;
        service.answer(first.attempt().id(), 2, 1, true);
        ExamSession again = service.startOrResume("E01").value;
        assertEquals(first.attempt().id(), again.attempt().id());
        assertEquals(1, again.answered());
        assertEquals(1, again.flagged());
        assertTrue(service.listTests().value.get(0).isInProgress());
        assertEquals(0, app.services.syncRepository().countPending(1));
    }

    @Test
    public void submit_scoresAndSendsWrongWordToBox1() {
        long id = service.startOrResume("E01").value.attempt().id();
        service.answer(id, 1, 1, false);
        service.answer(id, 2, 0, false);
        ExamSession result = service.submit(id).value;
        assertEquals(33, result.attempt().score());
        assertEquals(1, result.attempt().correct());
        // Câu 3 chưa làm tính sai, từ gắn với câu về hộp 1, ôn lại ngày mai (BR-055).
        assertEquals(1, app.number("SELECT box FROM card_progress WHERE word_id = 11"));
        assertEquals("2026-11-11", app.text("SELECT next_review_date FROM card_progress WHERE word_id = 11"));
        assertEquals(1, app.number("SELECT COUNT(*) FROM study_log WHERE activity_type = 'EXAM'"));
        assertEquals(1, app.number("SELECT xp_local FROM xp_events WHERE source_type = 'EXAM'"));
        assertEquals(1, service.history().value.size());
    }

    @Test
    public void submit_twice_countsOnce() {
        long id = service.startOrResume("E01").value.attempt().id();
        service.submit(id);
        service.submit(id);
        assertEquals(1, app.number("SELECT COUNT(*) FROM xp_events"));
        assertEquals(1, app.number("SELECT COUNT(*) FROM study_log"));
    }

    @Test
    public void answer_afterTimeUp_submitsAutomatically() {
        long id = service.startOrResume("E01").value.attempt().id();
        service.answer(id, 1, 1, false);
        app.sql("UPDATE exam_attempts SET started_at = started_at - 900000");
        Result<Void> late = service.answer(id, 2, 1, false);
        assertEquals(ErrorCode.TIME_UP, late.error);
        assertEquals(1, service.load(id).value.attempt().correct());
        assertEquals(900, service.load(id).value.attempt().durationSec());
    }

    @Test
    public void startOrResume_afterTimeUp_reportsOldAttemptThenStartsNew() {
        long id = service.startOrResume("E01").value.attempt().id();
        app.sql("UPDATE exam_attempts SET started_at = started_at - 900000");
        Result<ExamSession> late = service.startOrResume("E01");
        assertEquals(ErrorCode.TIME_UP, late.error);
        assertEquals(String.valueOf(id), late.detail);
        assertTrue(service.startOrResume("E01").value.attempt().id() != id);
    }

    @Test
    public void unknownTestOrAttempt_isNotFound() {
        assertEquals(ErrorCode.NOT_FOUND, service.startOrResume("E99").error);
        assertEquals(ErrorCode.NOT_FOUND, service.submit(99).error);
    }
}
