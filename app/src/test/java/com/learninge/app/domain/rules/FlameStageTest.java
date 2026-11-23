package com.learninge.app.domain.rules;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FlameStageTest {

    @Test
    public void of_boundaries() {
        int[][] expected = {{-1, 0}, {0, 0}, {1, 1}, {2, 1}, {3, 2}, {6, 2}, {7, 3}, {29, 3}, {30, 4}, {365, 4}};
        for (int[] pair : expected) {
            assertEquals("chuỗi " + pair[0], pair[1], FlameStage.of(pair[0]));
        }
    }

    @Test
    public void daysToNext_countsDownToMilestone() {
        assertEquals(1, FlameStage.daysToNext(0));
        assertEquals(2, FlameStage.daysToNext(5));
        assertEquals(1, FlameStage.daysToNext(29));
        assertEquals(0, FlameStage.daysToNext(30));
    }

    @Test
    public void milestone_matchesStage() {
        for (int stage = 1; stage <= FlameStage.MAX_STAGE; stage++) {
            assertEquals(stage, FlameStage.of(FlameStage.milestone(stage)));
        }
    }
}
