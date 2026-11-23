package com.learninge.app.data.model;

/**
 * Số liệu của Trang chủ (US-016).
 *
 * @param lastDeck bộ học gần nhất, null nếu chưa học bộ nào
 */
public record TodaySummary(String displayName, int streak, int learnedToday, int dailyGoal, int due, int newWords,
                           DeckSummary lastDeck) {
    private static final int SECONDS_PER_CARD = 30;

    public int cards() {
        return due + newWords;
    }

    public int estimatedMinutes() {
        return Math.max(1, (cards() * SECONDS_PER_CARD + 59) / 60);
    }

    public boolean hasMetGoal() {
        return learnedToday >= dailyGoal;
    }
}
