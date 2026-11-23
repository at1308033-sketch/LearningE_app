package com.learninge.app.domain.rules;

import java.time.LocalDate;

/**
 * Lịch ôn tập Leitner 5 hộp (BR-011, BR-012, BR-014). Java thuần.
 */
public final class LeitnerScheduler {
    public static final int MIN_BOX = 1;
    public static final int MAX_BOX = 5;

    // BR-012: khoảng ôn (ngày) theo hộp 1..5
    private static final int[] INTERVAL_DAYS = {1, 2, 4, 7, 14};
    private static final int FIRST_REMEMBERED_BOX = 2;

    /**
     * Trạng thái hiển thị của một từ (BR-014).
     */
    public enum Status {
        NEW, LEARNING, MASTERED
    }

    private LeitnerScheduler() {
    }

    /**
     * Hộp sau khi đánh giá. {@code currentBox = null} là thẻ Mới, đánh giá lần đầu.
     */
    public static int nextBox(Integer currentBox, boolean remembered) {
        if (!remembered) {
            return MIN_BOX;
        }
        return currentBox == null ? FIRST_REMEMBERED_BOX : Math.min(currentBox + 1, MAX_BOX);
    }

    public static LocalDate nextReviewDate(int box, LocalDate ratedOn) {
        return ratedOn.plusDays(intervalDays(box));
    }

    public static int intervalDays(int box) {
        if (box < MIN_BOX || box > MAX_BOX) {
            throw new IllegalArgumentException("box " + box);
        }
        return INTERVAL_DAYS[box - 1];
    }

    public static boolean isDue(LocalDate nextReviewDate, LocalDate today) {
        return !nextReviewDate.isAfter(today);
    }

    public static Status statusOf(Integer box) {
        if (box == null) {
            return Status.NEW;
        }
        return box >= MAX_BOX ? Status.MASTERED : Status.LEARNING;
    }

    /**
     * Số thẻ của phiên học từ mới (BR-010).
     */
    public static int newSessionSize(int dailyGoal, int learnedToday, int newWordsLeft) {
        return Math.min(Math.max(5, dailyGoal - learnedToday), newWordsLeft);
    }
}
