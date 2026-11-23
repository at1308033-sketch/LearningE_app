package com.learninge.app.domain.rules;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

/**
 * Điểm kinh nghiệm (BR-065..BR-069). Java thuần. Số trên máy chỉ là ước tính, server tính lại.
 */
public final class XpRules {
    public static final int DAILY_CAP = 500;
    /**
     * Ngày và tuần tính XP theo giờ Việt Nam, không theo múi giờ của máy.
     */
    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private static final int REMEMBERED_XP = 2;
    private static final int FORGOTTEN_XP = 1;
    private static final int PERFECT_QUIZ_BONUS = 5;

    private XpRules() {
    }

    public static int cards(int remembered, int forgotten) {
        return remembered * REMEMBERED_XP + forgotten * FORGOTTEN_XP;
    }

    public static int quiz(int correct, int total) {
        return correct + (total > 0 && correct == total ? PERFECT_QUIZ_BONUS : 0);
    }

    public static int exam(int correct) {
        return correct;
    }

    /**
     * XP được cộng sau khi áp trần ngày.
     */
    public static int capped(int xp, int earnedToday) {
        return Math.max(0, Math.min(xp, DAILY_CAP - earnedToday));
    }

    public static LocalDate dayOf(Instant instant) {
        return instant.atZone(ZONE).toLocalDate();
    }

    /**
     * Thứ Hai của tuần xếp hạng.
     */
    public static LocalDate weekStart(Instant instant) {
        return dayOf(instant).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * Thời điểm tuần xếp hạng kết thúc (00:00 thứ Hai kế tiếp, giờ Việt Nam).
     */
    public static Instant weekEnd(LocalDate weekStart) {
        return weekStart.plusWeeks(1).atStartOfDay(ZONE).toInstant();
    }
}
