package com.learninge.app.data.model;

/**
 * Đề thi thử kèm kết quả tốt nhất của người dùng.
 *
 * @param bestScore    null nếu chưa nộp lượt nào
 * @param isInProgress đang có lượt làm dở
 */
public record ExamTest(long id, String code, String examTag, String title, String partLabel, int questionCount,
                       int durationSec, Integer bestScore, boolean isInProgress) {
}
