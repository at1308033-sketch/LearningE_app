package com.learninge.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.learninge.app.TestApp;
import com.learninge.app.data.model.XpGain;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class XpServiceTest {
    private TestApp app;
    private XpService service;

    @Before
    public void open() {
        app = new TestApp();
        service = app.services.xpService();
    }

    @After
    public void close() {
        app.close();
    }

    @Test
    public void recordCards_savesPendingEvent() {
        assertEquals(17, service.recordCards("s", 7, 3).value.xp());
        assertEquals(1, app.number("SELECT COUNT(*) FROM xp_events WHERE dirty = 1 AND correct_count = 7 "
                + "AND wrong_count = 3 AND xp_local = 17 AND local_date = '2026-11-10'"));
        assertEquals(17, service.weekXp());
    }

    @Test
    public void record_nothingRated_savesNothing() {
        assertEquals(0, service.recordCards("s", 0, 0).value.xp());
        assertEquals(0, app.number("SELECT COUNT(*) FROM xp_events"));
    }

    @Test
    public void record_overDailyCap_isCutAndReported() {
        app.sql("INSERT INTO xp_events (uuid, user_id, source_type, correct_count, wrong_count, total_count, xp_local, "
                + "occurred_at, local_date, updated_at) VALUES ('x', 1, 'QUIZ', 1, 0, 1, 490, 0, '2026-11-10', 0)");
        XpGain gain = service.recordQuiz("q", 10, 10).value;
        assertEquals(10, gain.xp());
        assertTrue(gain.isCapped());
        assertEquals(0, service.recordExam("e", 5, 20).value.xp());
    }

    @Test
    public void weekXp_prefersServerNumberAndSkipsLastWeek() {
        app.sql("INSERT INTO xp_events (uuid, user_id, source_type, correct_count, wrong_count, total_count, xp_local, "
                + "xp_awarded, occurred_at, local_date, updated_at) VALUES "
                + "('a', 1, 'QUIZ', 1, 0, 1, 10, 4, 0, '2026-11-09', 0), ('b', 1, 'QUIZ', 1, 0, 1, 10, 10, 0, "
                + "'2026-11-08', 0)");
        assertEquals(4, service.weekXp());
        assertFalse(service.recordCards("s", 1, 0).value.isCapped());
    }
}
