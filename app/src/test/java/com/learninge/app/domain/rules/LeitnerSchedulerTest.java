package com.learninge.app.domain.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.learninge.app.domain.rules.LeitnerScheduler.Status;

import org.junit.Test;

import java.time.LocalDate;

public class LeitnerSchedulerTest {
    private static final LocalDate DAY = LocalDate.of(2026, 11, 10);

    @Test
    public void rate_newCard_goesToBox2OrBox1() {
        assertEquals(2, LeitnerScheduler.nextBox(null, true));
        assertEquals(1, LeitnerScheduler.nextBox(null, false));
    }

    @Test
    public void rate_remembered_movesUpOneBox() {
        assertEquals(2, LeitnerScheduler.nextBox(1, true));
        assertEquals(4, LeitnerScheduler.nextBox(3, true));
    }

    @Test
    public void rate_rememberedAtBox5_staysAtBox5() {
        assertEquals(5, LeitnerScheduler.nextBox(5, true));
    }

    @Test
    public void rate_forgotten_alwaysReturnsToBox1() {
        for (int box = 1; box <= 5; box++) {
            assertEquals(1, LeitnerScheduler.nextBox(box, false));
        }
    }

    @Test
    public void nextReviewDate_usesIntervalOfBox() {
        assertEquals(LocalDate.of(2026, 11, 11), LeitnerScheduler.nextReviewDate(1, DAY));
        assertEquals(LocalDate.of(2026, 11, 12), LeitnerScheduler.nextReviewDate(2, DAY));
        assertEquals(LocalDate.of(2026, 11, 14), LeitnerScheduler.nextReviewDate(3, DAY));
        assertEquals(LocalDate.of(2026, 11, 17), LeitnerScheduler.nextReviewDate(4, DAY));
        assertEquals(LocalDate.of(2026, 11, 24), LeitnerScheduler.nextReviewDate(5, DAY));
    }

    @Test
    public void intervalDays_rejectsUnknownBox() {
        assertThrows(IllegalArgumentException.class, () -> LeitnerScheduler.intervalDays(0));
        assertThrows(IllegalArgumentException.class, () -> LeitnerScheduler.intervalDays(6));
    }

    @Test
    public void isDue_whenDateIsTodayOrPast() {
        assertTrue(LeitnerScheduler.isDue(DAY, DAY));
        assertTrue(LeitnerScheduler.isDue(DAY.minusDays(1), DAY));
        assertFalse(LeitnerScheduler.isDue(DAY.plusDays(1), DAY));
    }

    @Test
    public void statusOf_followsBr014() {
        assertEquals(Status.NEW, LeitnerScheduler.statusOf(null));
        assertEquals(Status.LEARNING, LeitnerScheduler.statusOf(1));
        assertEquals(Status.LEARNING, LeitnerScheduler.statusOf(4));
        assertEquals(Status.MASTERED, LeitnerScheduler.statusOf(5));
    }

    @Test
    public void newSessionSize_followsBr010() {
        assertEquals(5, LeitnerScheduler.newSessionSize(10, 7, 20));
        assertEquals(10, LeitnerScheduler.newSessionSize(10, 0, 20));
        assertEquals(3, LeitnerScheduler.newSessionSize(10, 7, 3));
        assertEquals(5, LeitnerScheduler.newSessionSize(10, 30, 20));
        assertEquals(0, LeitnerScheduler.newSessionSize(10, 0, 0));
    }
}
