package com.learninge.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.learninge.app.TestApp;
import com.learninge.app.data.model.Statistics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class StatisticsServiceTest {
    private TestApp app;

    @Before
    public void open() {
        app = new TestApp();
        app.deck(1, "T01", null, "A1");
        app.word(11, 1, "mother");
        app.word(12, 1, "father");
        app.word(13, 1, "sister");
    }

    @After
    public void close() {
        app.close();
    }

    @Test
    public void load_emptyUser_hasZeros() {
        Statistics statistics = app.services.statisticsService().load().value;
        assertEquals(3, statistics.totalWords());
        assertEquals(3, statistics.newWords());
        assertEquals(List.of(0, 0, 0, 0, 0), statistics.boxCounts());
        assertNull(statistics.averageQuizScore());
        assertEquals(7, statistics.week().size());
    }

    @Test
    public void load_countsBoxesQuizAndWeek() {
        app.progress(11, 5, "2026-11-20");
        app.progress(12, 1, "2026-11-10");
        app.sql("INSERT INTO quiz_attempts (uuid, user_id, source_type, deck_id, score, correct_count, total_count, "
                + "duration_sec, created_at, updated_at) VALUES ('q1', 1, 'DECK', 1, 80, 8, 10, 60, "
                + app.services.clock().millis() + ", 0), ('q2', 1, 'DECK', 1, 10, 1, 10, 60, 0, 0)");
        app.sql("INSERT INTO study_log (uuid, user_id, activity_type, study_date, created_at, updated_at) "
                + "VALUES ('s1', 1, 'QUIZ', '2026-11-10', 0, 0), ('s2', 1, 'LEARN', '2026-11-04', 0, 0), "
                + "('s3', 1, 'LEARN', '2026-11-03', 0, 0)");
        Statistics statistics = app.services.statisticsService().load().value;
        assertEquals(List.of(1, 0, 0, 0, 1), statistics.boxCounts());
        assertEquals(1, statistics.mastered());
        assertEquals(1, statistics.learning());
        assertEquals(1, statistics.due());
        // Lượt quiz cũ hơn 7 ngày không được tính.
        assertEquals(80, (int) statistics.averageQuizScore());
        assertEquals(1, statistics.week().get(0).count());
        assertEquals(1, statistics.week().get(6).count());
    }

    @Test
    public void load_ignoresWordsOfOtherUsers() {
        app.user(2);
        app.deck(5, "d-5", 2L, null);
        app.word(50, 5, "secret");
        assertEquals(3, app.services.statisticsService().load().value.totalWords());
    }
}
