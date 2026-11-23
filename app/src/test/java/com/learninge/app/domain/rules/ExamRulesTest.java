package com.learninge.app.domain.rules;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ExamRulesTest {

    @Test
    public void score_rounds() {
        assertEquals(80, ExamRules.score(16, 20));
        assertEquals(33, ExamRules.score(1, 3));
        assertEquals(67, ExamRules.score(2, 3));
        assertEquals(0, ExamRules.score(0, 0));
    }

    @Test
    public void remainingMillis_countsFromStart() {
        assertEquals(900_000, ExamRules.remainingMillis(1_000, 900, 1_000));
        assertEquals(1, ExamRules.remainingMillis(0, 900, 899_999));
        assertEquals(0, ExamRules.remainingMillis(0, 900, 900_000));
        assertEquals(0, ExamRules.remainingMillis(0, 900, 5_000_000));
    }

    @Test
    public void usedSeconds_isCappedByDuration() {
        assertEquals(60, ExamRules.usedSeconds(0, 900, 60_500));
        assertEquals(900, ExamRules.usedSeconds(0, 900, 5_000_000));
        assertEquals(0, ExamRules.usedSeconds(10_000, 900, 0));
    }
}
