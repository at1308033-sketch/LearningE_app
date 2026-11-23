package com.learninge.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.learninge.app.TestApp;
import com.learninge.app.data.model.ActivityType;
import com.learninge.app.data.model.TodaySummary;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProgressServiceTest {
    private TestApp app;
    private ProgressService service;

    @Before
    public void open() {
        app = new TestApp();
        service = app.services.progressService();
        app.deck(1, "T01", null, "A1");
        for (int i = 0; i < 8; i++) {
            app.word(10 + i, 1, "word" + i);
        }
    }

    @After
    public void close() {
        app.close();
    }

    @Test
    public void today_newUser_hasNoDeckAndZeroStreak() {
        TodaySummary today = service.today().value;
        assertEquals("Minh", today.displayName());
        assertEquals(0, today.streak());
        assertNull(today.lastDeck());
        assertEquals(10, today.newWords());
        assertFalse(today.hasMetGoal());
    }

    @Test
    public void record_twiceOnSameDay_countsOneDay() {
        assertEquals(1, service.record(ActivityType.LEARN, "T01").value.current);
        assertEquals(1, service.record(ActivityType.REVIEW, null).value.current);
        assertEquals(2, app.number("SELECT COUNT(*) FROM study_log WHERE dirty = 1"));
        assertEquals(1, app.number("SELECT current_streak FROM user_settings WHERE user_id = 1"));
    }

    @Test
    public void record_continuesStreakFromYesterday() {
        app.sql("INSERT INTO study_log (uuid, user_id, activity_type, study_date, created_at, updated_at) "
                + "VALUES ('s', 1, 'QUIZ', '2026-11-09', 0, 0)");
        assertEquals(2, service.record(ActivityType.LEARN, "T01").value.current);
        assertEquals(2, service.today().value.streak());
    }

    @Test
    public void today_afterMissingOneDay_showsZero() {
        app.sql("INSERT INTO study_log (uuid, user_id, activity_type, study_date, created_at, updated_at) "
                + "VALUES ('s', 1, 'QUIZ', '2026-11-08', 0, 0)");
        assertEquals(0, service.today().value.streak());
    }

    @Test
    public void today_continuesLastDeckWithWordsLeft() {
        service.record(ActivityType.LEARN, "T01");
        app.progress(10, 2, TestApp.TODAY);
        app.sql("UPDATE card_progress SET first_learned_date = '" + TestApp.TODAY + "'");
        TodaySummary today = service.today().value;
        assertEquals("T01", today.lastDeck().deck().code());
        assertEquals(1, today.learnedToday());
        assertEquals(1, today.due());
        // Mục tiêu còn 9 từ nhưng bộ chỉ còn 7 từ mới (BR-010).
        assertEquals(7, today.newWords());
        assertTrue(today.estimatedMinutes() > 0);
    }
}
