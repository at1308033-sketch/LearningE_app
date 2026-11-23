package com.learninge.app.domain;

import android.database.SQLException;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.core.SessionStore;
import com.learninge.app.data.dao.CardProgressDao;
import com.learninge.app.data.dao.StatisticsDao;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.data.model.Statistics;
import com.learninge.app.data.model.Statistics.DayActivity;
import com.learninge.app.data.repo.ProgressRepository;
import com.learninge.app.domain.rules.StreakCalculator.Streak;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Số liệu màn Tiến độ ({@code ProgressService.statistics()} trong API-SPECIFICATION). Gọi trên {@code dbRead}.
 */
public class StatisticsService {
    private static final int WEEK_DAYS = 7;
    private static final int TOP_BOX = 5;

    private final StatisticsDao statistics;
    private final CardProgressDao cards;
    private final ProgressRepository progress;
    private final DeckService decks;
    private final SessionStore session;
    private final Clock clock;

    public StatisticsService(StatisticsDao statistics, CardProgressDao cards, ProgressRepository progress,
                             DeckService decks, SessionStore session, Clock clock) {
        this.statistics = statistics;
        this.cards = cards;
        this.progress = progress;
        this.decks = decks;
        this.session = session;
        this.clock = clock;
    }

    public Result<Statistics> load() {
        long userId = session.userId();
        LocalDate today = LocalDate.now(clock);
        LocalDate firstDay = today.minusDays(WEEK_DAYS - 1);
        try {
            Result<List<DeckSummary>> topics = decks.listTopics(null);
            if (!topics.isOk()) {
                return Result.errorOf(topics);
            }
            Streak streak = progress.streakOf(userId);
            List<Integer> boxes = statistics.countByBox(userId);
            int started = boxes.stream().mapToInt(Integer::intValue).sum();
            int mastered = boxes.get(TOP_BOX - 1);
            long weekStart = firstDay.atStartOfDay(clock.getZone()).toInstant().toEpochMilli();
            return Result.ok(new Statistics(streak.displayOn(today), streak.longest, statistics.countWords(userId),
                    mastered, started - mastered, cards.countDue(userId, today.toString()),
                    statistics.averageQuizScore(userId, weekStart), boxes, week(userId, firstDay), topics.value));
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }

    private List<DayActivity> week(long userId, LocalDate firstDay) {
        Map<String, Integer> counts = statistics.countActivities(userId, firstDay.toString());
        List<DayActivity> days = new ArrayList<>();
        for (int i = 0; i < WEEK_DAYS; i++) {
            LocalDate day = firstDay.plusDays(i);
            days.add(new DayActivity(day, counts.getOrDefault(day.toString(), 0)));
        }
        return days;
    }
}
