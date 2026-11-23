package com.learninge.app.data.model;

/**
 * Lượt thi. {@code score}, {@code correct}, {@code durationSec} chỉ có nghĩa khi đã nộp.
 */
public record ExamAttempt(long id, String uuid, ExamTest test, boolean isSubmitted, long startedAt, long submittedAt,
                          int score, int correct, int total, int durationSec) {

    public long endsAt() {
        return startedAt + test.durationSec() * 1_000L;
    }
}
