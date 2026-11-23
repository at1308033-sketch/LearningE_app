package com.learninge.app.data.model;

import static org.junit.Assert.assertEquals;

import com.learninge.app.data.model.Leaderboard.Entry;

import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

public class LeaderboardTest {

    @Test
    public void xpToNextRank_isOneMoreThanTheGap() {
        assertEquals(22, board(entry(6, 361), entry(7, 340)).xpToNextRank());
    }

    @Test
    public void xpToNextRank_zeroWhenFirstUnrankedOrOutsideTop() {
        assertEquals(0, board(entry(1, 340)).xpToNextRank());
        assertEquals(0, board(entry(0, 0)).xpToNextRank());
        assertEquals(0, new Leaderboard(LocalDate.of(2026, 11, 9), List.of(entry(50, 400)), entry(128, 5), 0, false)
                .xpToNextRank());
    }

    private static Leaderboard board(Entry... entries) {
        return new Leaderboard(LocalDate.of(2026, 11, 9), List.of(entries), entries[entries.length - 1], 0, false);
    }

    private static Entry entry(int rank, int xp) {
        return new Entry(rank, "n" + rank, xp, 0, false);
    }
}
