package com.learninge.app.domain.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.learninge.app.domain.rules.StreakCalculator.Streak;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

public class StreakCalculatorTest {

    private static LocalDate nov(int day) {
        return LocalDate.of(2026, 11, day);
    }

    private static Streak of(LocalDate... days) {
        return StreakCalculator.of(Arrays.asList(days));
    }

    @Test
    public void of_noDays_isZero() {
        Streak streak = StreakCalculator.of(Collections.emptyList());
        assertEquals(0, streak.current);
        assertEquals(0, streak.longest);
        assertNull(streak.lastStudyDate);
        assertEquals(0, streak.displayOn(nov(11)));
    }

    @Test
    public void of_gap_restartsAtOneAndKeepsLongest() {
        Streak streak = of(nov(8), nov(9), nov(11));
        assertEquals(1, streak.current);
        assertEquals(2, streak.longest);
        assertEquals(nov(11), streak.lastStudyDate);
    }

    @Test
    public void of_mergesDaysFromTwoDevicesInAnyOrder() {
        assertEquals(2, of(nov(9), nov(8), nov(8)).current);
    }

    @Test
    public void of_crossesMonthBoundary() {
        assertEquals(2, of(LocalDate.of(2026, 10, 31), nov(1)).current);
    }

    @Test
    public void displayOn_keepsStreakUntilOneFullDayIsMissed() {
        Streak streak = of(nov(8), nov(9));
        assertEquals(2, streak.displayOn(nov(9)));
        assertEquals(2, streak.displayOn(nov(10)));
        assertEquals(0, streak.displayOn(nov(11)));
    }
}
