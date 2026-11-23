package com.learninge.app.data.model;

/**
 * Bộ từ kèm số từ theo trạng thái (BR-014) và số thẻ đến hạn của người dùng hiện tại.
 */
public record DeckSummary(Deck deck, int total, int mastered, int learning, int due) {

    public int newWords() {
        return total - mastered - learning;
    }
}
