package com.learninge.app.domain;

import android.database.SQLException;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.core.SessionStore;
import com.learninge.app.data.dao.ExamDao;
import com.learninge.app.data.model.ActivityType;
import com.learninge.app.data.model.ExamAttempt;
import com.learninge.app.data.model.ExamQuestion;
import com.learninge.app.data.model.ExamSession;
import com.learninge.app.data.model.ExamTest;
import com.learninge.app.data.repo.ExamRepository;
import com.learninge.app.domain.rules.ExamRules;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

/**
 * Thi thử (API-SPECIFICATION §3.5). Phương thức đọc gọi trên {@code dbRead}, còn lại trên {@code dbWrite}.
 */
public class ExamService {
    private final ExamDao exams;
    private final ExamRepository repository;
    private final ProgressService progress;
    private final XpService xp;
    private final SessionStore session;
    private final Clock clock;

    public ExamService(ExamDao exams, ExamRepository repository, ProgressService progress, XpService xp,
                       SessionStore session, Clock clock) {
        this.exams = exams;
        this.repository = repository;
        this.progress = progress;
        this.xp = xp;
        this.session = session;
        this.clock = clock;
    }

    public Result<List<ExamTest>> listTests() {
        return guarded(() -> Result.ok(exams.listTests(session.userId())));
    }

    public Result<List<ExamAttempt>> history() {
        return guarded(() -> Result.ok(exams.listSubmitted(session.userId())));
    }

    /**
     * Bắt đầu lượt mới hoặc tiếp tục lượt đang làm. Lượt làm dở đã hết giờ thì tự nộp và báo TIME_UP, {@code detail} là
     * khóa của lượt đó (AC-036.3, AC-036.4).
     */
    public Result<ExamSession> startOrResume(String testCode) {
        return guarded(() -> {
            ExamTest test = exams.findTest(session.userId(), testCode);
            if (test == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
            ExamAttempt current = exams.findInProgress(session.userId(), test.id());
            if (current != null && isTimeUp(current)) {
                return timeUp(current);
            }
            long attemptId = current != null ? current.id()
                    : exams.insertAttempt(session.userId(), test, clock.millis());
            return load(attemptId);
        });
    }

    /**
     * Lượt thi kèm câu hỏi và câu trả lời.
     */
    public Result<ExamSession> load(long attemptId) {
        return guarded(() -> {
            ExamAttempt attempt = exams.findAttempt(session.userId(), attemptId);
            return attempt == null ? Result.error(ErrorCode.NOT_FOUND)
                    : Result.ok(new ExamSession(attempt, exams.listQuestions(attempt)));
        });
    }

    /**
     * Lưu ngay từng câu (DEC-010); hết giờ thì tự nộp và báo TIME_UP.
     */
    public Result<Void> answer(long attemptId, int number, Integer selectedIndex, boolean isFlagged) {
        return guarded(() -> {
            ExamAttempt attempt = exams.findAttempt(session.userId(), attemptId);
            if (attempt == null || attempt.isSubmitted()) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
            if (isTimeUp(attempt)) {
                return timeUp(attempt);
            }
            for (ExamQuestion question : exams.listQuestions(attempt)) {
                if (question.number() == number) {
                    exams.saveAnswer(attemptId, question.id(), selectedIndex, isFlagged);
                    return Result.ok(null);
                }
            }
            return Result.error(ErrorCode.NOT_FOUND);
        });
    }

    /**
     * Nộp bài; nộp lại lượt đã nộp thì chỉ trả kết quả cũ.
     */
    public Result<ExamSession> submit(long attemptId) {
        return guarded(() -> {
            ExamAttempt attempt = exams.findAttempt(session.userId(), attemptId);
            if (attempt == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
            if (!attempt.isSubmitted()) {
                grade(attempt);
            }
            return load(attemptId);
        });
    }

    public long remainingMillis(ExamAttempt attempt) {
        return ExamRules.remainingMillis(attempt.startedAt(), attempt.test().durationSec(), clock.millis());
    }

    private void grade(ExamAttempt attempt) {
        // Chấm bài, nhật ký học và XP cùng một giao dịch.
        repository.inTransaction(() -> {
            int correct = repository.submit(session.userId(), attempt, LocalDate.now(clock), clock.millis());
            progress.record(ActivityType.EXAM, attempt.uuid());
            xp.recordExam(attempt.uuid(), correct, attempt.total());
            return null;
        });
    }

    private boolean isTimeUp(ExamAttempt attempt) {
        return remainingMillis(attempt) == 0;
    }

    private <T> Result<T> timeUp(ExamAttempt attempt) {
        grade(attempt);
        return Result.error(ErrorCode.TIME_UP, String.valueOf(attempt.id()));
    }

    private static <T> Result<T> guarded(Supplier<Result<T>> body) {
        try {
            return body.get();
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }
}
