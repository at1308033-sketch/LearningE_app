package com.learninge.app.data.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Số liệu màn Tiến độ (US-017), tính trực tiếp từ dữ liệu trên máy (BR-033).
 *
 * @param averageQuizScore điểm quiz trung bình 7 ngày, null nếu chưa làm quiz nào
 * @param boxCounts        số thẻ ở hộp 1..5 (BR-071)
 */
public record Statistics(int streak, int longestStreak, int totalWords, int mastered, int learning, int due,
                         Integer averageQuizScore, List<Integer> boxCounts, List<DayActivity> week,
                         List<DeckSummary> topics) {

    public record DayActivity(LocalDate day, int count) {
    }

    public int newWords() {
        return totalWords - mastered - learning;
    }
}
