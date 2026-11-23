package com.learninge.app.domain;

import android.database.SQLException;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.core.SessionStore;
import com.learninge.app.data.dao.XpDao;
import com.learninge.app.data.model.XpGain;
import com.learninge.app.data.model.XpSource;
import com.learninge.app.domain.rules.XpRules;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Ghi sự kiện XP sau phiên học, quiz, thi thử (US-043). Phương thức ghi gọi trên {@code dbWrite}.
 */
public class XpService {
    private final XpDao events;
    private final SessionStore session;
    private final Clock clock;

    public XpService(XpDao events, SessionStore session, Clock clock) {
        this.events = events;
        this.session = session;
        this.clock = clock;
    }

    public Result<XpGain> recordCards(String sessionUuid, int remembered, int forgotten) {
        return record(XpSource.CARD_SESSION, sessionUuid, remembered, forgotten, XpRules.cards(remembered, forgotten));
    }

    public Result<XpGain> recordQuiz(String attemptUuid, int correct, int total) {
        return record(XpSource.QUIZ, attemptUuid, correct, total - correct, XpRules.quiz(correct, total));
    }

    public Result<XpGain> recordExam(String attemptUuid, int correct, int total) {
        return record(XpSource.EXAM, attemptUuid, correct, total - correct, XpRules.exam(correct));
    }

    /**
     * XP từ đầu tuần xếp hạng theo dữ liệu trên máy.
     */
    public int weekXp() {
        return events.sumFrom(session.userId(), XpRules.weekStart(clock.instant()));
    }

    private Result<XpGain> record(XpSource source, String refUuid, int correct, int wrong, int xp) {
        if (correct + wrong == 0) {
            return Result.ok(XpGain.NONE);
        }
        long userId = session.userId();
        LocalDate day = XpRules.dayOf(clock.instant());
        try {
            int earnedToday = events.sumFrom(userId, day);
            int gained = XpRules.capped(xp, earnedToday);
            events.insert(userId, source, refUuid, correct, wrong, gained, day, clock.millis());
            return Result.ok(new XpGain(gained, earnedToday + gained >= XpRules.DAILY_CAP));
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }
}
