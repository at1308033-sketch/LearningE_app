package com.learninge.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.learninge.app.TestApp;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.data.model.ActivityType;
import com.learninge.app.data.model.SessionSummary;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class StudyServiceTest {
    private TestApp app;
    private StudyService service;

    @Before
    public void open() {
        app = new TestApp();
        service = app.services.studyService();
        app.deck(1, "T01", null, "A1");
        for (int i = 0; i < 12; i++) {
            app.word(10 + i, 1, "word" + i);
        }
    }

    @After
    public void close() {
        app.close();
    }

    @Test
    public void newSession_takesRemainingGoal() {
        assertEquals(10, service.newSession(1).value.size());
        for (int i = 0; i < 8; i++) {
            service.rate(10 + i, true);
        }
        // Mục tiêu còn 2 nhưng phiên tối thiểu 5, bộ chỉ còn 4 từ mới (BR-010).
        assertEquals(4, service.newSession(1).value.size());
    }

    @Test
    public void newSession_withoutNewWords_isNothingToStudy() {
        for (int i = 0; i < 12; i++) {
            service.rate(10 + i, false);
        }
        assertEquals(ErrorCode.NOTHING_TO_STUDY, service.newSession(1).error);
    }

    @Test
    public void rate_twice_keepsOneRowAndMovesBox() {
        assertEquals(2, (int) service.rate(10, true).value);
        assertEquals(3, (int) service.rate(10, true).value);
        assertEquals(1, app.number("SELECT COUNT(*) FROM card_progress"));
        assertEquals(2, app.number("SELECT review_count FROM card_progress"));
        assertEquals("2026-11-14", app.text("SELECT next_review_date FROM card_progress"));
        assertEquals(TestApp.TODAY, app.text("SELECT first_learned_date FROM card_progress"));
        assertEquals(1, app.number("SELECT dirty FROM card_progress"));
    }

    @Test
    public void rate_forgotten_returnsToBox1Tomorrow() {
        app.progress(10, 4, TestApp.TODAY);
        assertEquals(1, (int) service.rate(10, false).value);
        assertEquals("2026-11-11", app.text("SELECT next_review_date FROM card_progress"));
        // Ngày học lần đầu không đổi khi ôn lại.
        assertEquals("2026-11-01", app.text("SELECT first_learned_date FROM card_progress"));
    }

    @Test
    public void reviewSession_ordersByDateThenBoxAndSkipsFuture() {
        app.progress(10, 3, "2026-11-10");
        app.progress(11, 1, "2026-11-10");
        app.progress(12, 5, "2026-11-08");
        app.progress(13, 2, "2026-11-11");
        List<Long> ids = service.reviewSession().value.stream().map(card -> card.item().word().id())
                .collect(java.util.stream.Collectors.toList());
        assertEquals(List.of(12L, 11L, 10L), ids);
    }

    @Test
    public void reviewSession_isLimitedTo50() {
        app.deck(2, "T02", null, "A1");
        for (int i = 0; i < 60; i++) {
            app.word(100 + i, 2, "w" + i);
            app.progress(100 + i, 1, "2026-11-09");
        }
        assertEquals(50, service.reviewSession().value.size());
    }

    @Test
    public void finishSession_recordsActivityAndStreak() {
        List<Integer> before = service.boxCounts();
        service.rate(10, true);
        service.rate(11, false);
        SessionSummary summary = service.finishSession(ActivityType.LEARN, 1, 1, 1, 0, before).value;
        assertEquals(50, summary.percent());
        assertEquals(1, summary.streak());
        assertEquals(List.of(1, 1, 0, 0, 0), summary.boxesAfter());
        assertTrue(summary.displayName().startsWith("Minh"));
        assertEquals("T01", app.text("SELECT ref_uuid FROM study_log WHERE activity_type = 'LEARN'"));
    }
}
