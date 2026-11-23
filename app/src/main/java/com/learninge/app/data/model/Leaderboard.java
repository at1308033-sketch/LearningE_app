package com.learninge.app.data.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Bảng xếp hạng tuần (US-042).
 *
 * @param fetchedAt thời điểm tải về máy (epoch millis)
 * @param isCached  true nếu đang hiện bản đã lưu vì không tải được
 */
public record Leaderboard(LocalDate weekStart, List<Entry> entries, Entry me, long fetchedAt, boolean isCached) {

    /**
     * @param rank 0 = chưa có hạng (0 XP tuần này)
     */
    public record Entry(int rank, String nickname, int xp, int streak, boolean isMe) {
    }

    /**
     * XP còn thiếu để vượt người ngay trên; 0 nếu đứng đầu, chưa có hạng hoặc người trên ngoài top 50.
     */
    public int xpToNextRank() {
        return entries.stream().filter(entry -> me.rank() > 1 && entry.rank() == me.rank() - 1)
                .mapToInt(entry -> entry.xp() - me.xp() + 1).findFirst().orElse(0);
    }
}
