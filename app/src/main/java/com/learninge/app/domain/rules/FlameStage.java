package com.learninge.app.domain.rules;

/**
 * Giai đoạn ngọn lửa theo chuỗi ngày (BR-070). Java thuần.
 */
public final class FlameStage {
    public static final int MAX_STAGE = 4;

    // Chuỗi ngày nhỏ nhất của giai đoạn 1..4
    private static final int[] MILESTONES = {1, 3, 7, 30};

    private FlameStage() {
    }

    public static int of(int streak) {
        int stage = 0;
        while (stage < MAX_STAGE && streak >= MILESTONES[stage]) {
            stage++;
        }
        return stage;
    }

    /**
     * Số ngày cần có để vào giai đoạn 1..4.
     */
    public static int milestone(int stage) {
        return MILESTONES[stage - 1];
    }

    /**
     * Số ngày còn thiếu để lên giai đoạn kế tiếp; 0 khi đã ở giai đoạn cao nhất.
     */
    public static int daysToNext(int streak) {
        int stage = of(streak);
        return stage == MAX_STAGE ? 0 : MILESTONES[stage] - Math.max(0, streak);
    }
}
