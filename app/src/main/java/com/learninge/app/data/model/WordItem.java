package com.learninge.app.data.model;

import com.learninge.app.domain.rules.LeitnerScheduler;

/**
 * Từ kèm tiến độ của người dùng hiện tại; {@code box} null = Mới.
 */
public record WordItem(Word word, Integer box, String nextReviewDate, boolean isFavorite) {

    public LeitnerScheduler.Status status() {
        return LeitnerScheduler.statusOf(box);
    }
}
