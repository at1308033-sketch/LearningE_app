package com.learninge.app.domain.rules;

import java.time.LocalDate;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Chuỗi ngày học (BR-031). Tính lại từ toàn bộ ngày học nên không phụ thuộc máy nào đồng bộ trước.
 */
public final class StreakCalculator {

    /**
     * Kết quả bất biến.
     */
    public static final class Streak {
        public final int current;
        public final int longest;
        /**
         * null nếu chưa học ngày nào.
         */
        public final LocalDate lastStudyDate;

        Streak(int current, int longest, LocalDate lastStudyDate) {
            this.current = current;
            this.longest = longest;
            this.lastStudyDate = lastStudyDate;
        }

        /**
         * Chuỗi để hiển thị hôm nay: nghỉ quá một ngày thì chuỗi đã đứt, dù chưa có hoạt động mới để tính lại.
         */
        public int displayOn(LocalDate today) {
            boolean alive = lastStudyDate != null && !lastStudyDate.isBefore(today.minusDays(1));
            return alive ? current : 0;
        }
    }

    private StreakCalculator() {
    }

    public static Streak of(Collection<LocalDate> studyDays) {
        int current = 0;
        int longest = 0;
        LocalDate last = null;
        for (LocalDate day : new TreeSet<>(studyDays)) {
            current = last != null && last.plusDays(1).equals(day) ? current + 1 : 1;
            longest = Math.max(longest, current);
            last = day;
        }
        return new Streak(current, longest, last);
    }
}
