package com.learninge.app.domain;

import android.database.SQLException;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.core.SessionStore;
import com.learninge.app.data.dao.CardProgressDao;
import com.learninge.app.data.dao.DeckDao;
import com.learninge.app.data.dao.SettingsDao;
import com.learninge.app.data.dao.UserDao;
import com.learninge.app.data.model.ActivityType;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.data.model.Settings;
import com.learninge.app.data.model.TodaySummary;
import com.learninge.app.data.model.User;
import com.learninge.app.data.repo.ProgressRepository;
import com.learninge.app.domain.rules.LeitnerScheduler;
import com.learninge.app.domain.rules.StreakCalculator.Streak;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Tiến độ học và chuỗi ngày (API-SPECIFICATION §3.7). Mọi số tính trực tiếp từ dữ liệu trên máy (BR-033).
 */
public class ProgressService {
    private final ProgressRepository progress;
    private final CardProgressDao cards;
    private final DeckDao decks;
    private final SettingsDao settings;
    private final UserDao users;
    private final SessionStore session;
    private final Clock clock;

    public ProgressService(ProgressRepository progress, CardProgressDao cards, DeckDao decks, SettingsDao settings,
                           UserDao users, SessionStore session, Clock clock) {
        this.progress = progress;
        this.cards = cards;
        this.decks = decks;
        this.settings = settings;
        this.users = users;
        this.session = session;
        this.clock = clock;
    }

    /**
     * Ghi một hoạt động học hoàn thành (BR-032); trả chuỗi ngày mới. Gọi trên {@code dbWrite}.
     */
    public Result<Streak> record(ActivityType type, String refUuid) {
        try {
            return Result.ok(progress.record(session.userId(), type, refUuid, LocalDate.now(clock), clock.millis()));
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }

    /**
     * Chuỗi ngày tính đến hôm nay.
     */
    public Result<Integer> currentStreak() {
        try {
            return Result.ok(progress.streakOf(session.userId()).displayOn(LocalDate.now(clock)));
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }

    /**
     * Số liệu Trang chủ (US-016). Gọi trên {@code dbRead}.
     */
    public Result<TodaySummary> today() {
        long userId = session.userId();
        LocalDate today = LocalDate.now(clock);
        try {
            User user = users.findById(userId);
            Settings config = settings.find(userId);
            if (user == null || config == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
            Long deckId = progress.findLastDeckId(userId);
            DeckSummary lastDeck = deckId == null ? null : decks.findSummary(userId, today.toString(), deckId);
            int learned = cards.countLearnedOn(userId, today.toString());
            return Result.ok(new TodaySummary(user.displayName(), progress.streakOf(userId).displayOn(today), learned,
                    config.dailyGoal(), cards.countDue(userId, today.toString()),
                    plannedNewWords(config.dailyGoal(), learned, lastDeck), lastDeck));
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }

    // Chưa học bộ nào thì chưa biết sẽ học từ nào, nên số từ mới lấy theo mục tiêu còn lại.
    private static int plannedNewWords(int goal, int learned, DeckSummary deck) {
        if (learned >= goal) {
            return 0;
        }
        return deck == null ? goal - learned : LeitnerScheduler.newSessionSize(goal, learned, deck.newWords());
    }
}
