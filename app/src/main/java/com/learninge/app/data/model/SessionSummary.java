package com.learninge.app.data.model;

import java.util.List;

/**
 * Kết quả một phiên học thẻ (SCR-10).
 *
 * @param deckId      bộ vừa học, -1 với phiên ôn tập
 * @param boxesBefore số thẻ ở hộp 1..5 trước phiên (BR-071)
 */
public record SessionSummary(ActivityType kind, long deckId, String displayName, int remembered, int forgotten,
                             int streakBefore, int streak, List<Integer> boxesBefore, List<Integer> boxesAfter) {

    public int total() {
        return remembered + forgotten;
    }

    public int percent() {
        return total() == 0 ? 0 : Math.round(remembered * 100f / total());
    }
}
