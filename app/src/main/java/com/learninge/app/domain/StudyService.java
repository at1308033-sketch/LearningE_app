package com.learninge.app.domain;

import android.database.SQLException;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.core.SessionStore;
import com.learninge.app.data.dao.CardProgressDao;
import com.learninge.app.data.dao.StatisticsDao;
import com.learninge.app.data.dao.UserDao;
import com.learninge.app.data.dao.WordDao;
import com.learninge.app.data.model.ActivityType;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.data.model.SessionSummary;
import com.learninge.app.data.model.User;
import com.learninge.app.data.model.WordDetail;
import com.learninge.app.data.model.WordItem;
import com.learninge.app.data.repo.StudyRepository;
import com.learninge.app.domain.rules.LeitnerScheduler;
import com.learninge.app.domain.rules.StreakCalculator.Streak;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Phiên học thẻ (API-SPECIFICATION §3.3): học từ mới và ôn tập.
 */
public class StudyService {
    public static final int MAX_REVIEW_CARDS = 50;
    public static final long NO_DECK = -1;

    private final WordDao words;
    private final CardProgressDao cards;
    private final StudyRepository study;
    private final StatisticsDao statistics;
    private final UserDao users;
    private final DeckService decks;
    private final ProgressService progress;
    private final SessionStore session;
    private final Clock clock;

    public StudyService(WordDao words, CardProgressDao cards, StudyRepository study, StatisticsDao statistics,
                        UserDao users, DeckService decks, ProgressService progress, SessionStore session,
                        Clock clock) {
        this.words = words;
        this.cards = cards;
        this.study = study;
        this.statistics = statistics;
        this.users = users;
        this.decks = decks;
        this.progress = progress;
        this.session = session;
        this.clock = clock;
    }

    /**
     * Các từ Mới của bộ, số lượng theo BR-010. Gọi trên {@code dbRead}.
     */
    public Result<List<WordDetail>> newSession(long deckId) {
        try {
            Result<DeckSummary> deck = decks.getDeck(deckId);
            if (!deck.isOk()) {
                return Result.errorOf(deck);
            }
            int size = LeitnerScheduler.newSessionSize(decks.dailyGoal(),
                    cards.countLearnedOn(session.userId(), LocalDate.now(clock).toString()), deck.value.newWords());
            return cardsOf(words.listNew(session.userId(), deckId, size));
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }

    /**
     * Thẻ đến hạn, sớm nhất trước, tối đa 50 (BR-013). Gọi trên {@code dbRead}.
     */
    public Result<List<WordDetail>> reviewSession() {
        try {
            return cardsOf(words.listDue(session.userId(), LocalDate.now(clock).toString(), MAX_REVIEW_CARDS));
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }

    /**
     * Số thẻ ở hộp 1..5, để so trước và sau phiên (BR-071).
     */
    public List<Integer> boxCounts() {
        return statistics.countByBox(session.userId());
    }

    public int currentStreak() {
        Result<Integer> streak = progress.currentStreak();
        return streak.isOk() ? streak.value : 0;
    }

    /**
     * Đánh giá một thẻ; lưu ngay nên thoát giữa phiên không mất (AC-009.5). Gọi trên {@code dbWrite}.
     */
    public Result<Integer> rate(long wordId, boolean remembered) {
        try {
            return Result.ok(study.rate(session.userId(), wordId, remembered, LocalDate.now(clock), clock.millis()));
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }

    /**
     * Kết thúc phiên: ghi hoạt động học và chuỗi ngày (BR-031, BR-032). Gọi trên {@code dbWrite}.
     */
    public Result<SessionSummary> finishSession(ActivityType kind, long deckId, int remembered, int forgotten,
                                                int streakBefore, List<Integer> boxesBefore) {
        Result<DeckSummary> deck = deckId == NO_DECK ? null : decks.getDeck(deckId);
        Result<Streak> streak = progress.record(kind, deck == null || !deck.isOk() ? null : deck.value.deck().uuid());
        if (!streak.isOk()) {
            return Result.errorOf(streak);
        }
        User user = users.findById(session.userId());
        return Result.ok(new SessionSummary(kind, deckId, user == null ? "" : user.displayName(), remembered, forgotten,
                streakBefore, streak.value.current, boxesBefore, boxCounts()));
    }

    private Result<List<WordDetail>> cardsOf(List<WordItem> items) {
        List<WordDetail> result = new ArrayList<>();
        for (WordItem item : items) {
            Result<WordDetail> card = decks.getWord(item.word().id());
            if (!card.isOk()) {
                return Result.errorOf(card);
            }
            result.add(card.value);
        }
        return result.isEmpty() ? Result.error(ErrorCode.NOTHING_TO_STUDY) : Result.ok(result);
    }
}
