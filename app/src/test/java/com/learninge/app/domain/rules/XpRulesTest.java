package com.learninge.app.domain.rules;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;

public class XpRulesTest {

    @Test
    public void cards_followsBr065() {
        assertEquals(17, XpRules.cards(7, 3));
        assertEquals(0, XpRules.cards(0, 0));
    }

    @Test
    public void quiz_followsBr066() {
        assertEquals(15, XpRules.quiz(10, 10));
        assertEquals(7, XpRules.quiz(7, 10));
        assertEquals(0, XpRules.quiz(0, 0));
    }

    @Test
    public void exam_hasNoPerfectBonus() {
        assertEquals(20, XpRules.exam(20));
    }

    @Test
    public void capped_stopsAt500PerDay() {
        assertEquals(10, XpRules.capped(15, 490));
        assertEquals(0, XpRules.capped(15, 500));
        assertEquals(0, XpRules.capped(15, 510));
        assertEquals(15, XpRules.capped(15, 0));
    }

    @Test
    public void week_startsMondayInVietnam() {
        // 23:59 Chủ nhật 15/11 và 00:00 thứ Hai 16/11 giờ Việt Nam.
        assertEquals(LocalDate.of(2026, 11, 9), XpRules.weekStart(Instant.parse("2026-11-15T16:59:59Z")));
        assertEquals(LocalDate.of(2026, 11, 16), XpRules.weekStart(Instant.parse("2026-11-15T17:00:00Z")));
        assertEquals(Instant.parse("2026-11-15T17:00:00Z"), XpRules.weekEnd(LocalDate.of(2026, 11, 9)));
    }
}
