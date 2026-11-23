package com.learninge.app.domain.rules;

/**
 * Tính giờ và chấm điểm thi thử (BR-054). Java thuần.
 */
public final class ExamRules {

    private ExamRules() {
    }

    /**
     * Điểm 0..100, làm tròn; câu chưa làm tính sai.
     */
    public static int score(int correct, int total) {
        return total == 0 ? 0 : (int) Math.round(correct * 100.0 / total);
    }

    /**
     * Mili giây còn lại, tính từ lúc bắt đầu nên tắt app không dừng đồng hồ (DEC-010).
     */
    public static long remainingMillis(long startedAt, int durationSec, long now) {
        return Math.max(0, startedAt + durationSec * 1_000L - now);
    }

    /**
     * Thời gian đã làm, không vượt thời lượng của đề.
     */
    public static int usedSeconds(long startedAt, int durationSec, long now) {
        return (int) Math.max(0, Math.min(durationSec, (now - startedAt) / 1_000));
    }
}
