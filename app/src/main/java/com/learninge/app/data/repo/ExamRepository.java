package com.learninge.app.data.repo;

import com.learninge.app.data.dao.CardProgressDao;
import com.learninge.app.data.dao.ExamDao;
import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.Tx;
import com.learninge.app.data.model.ExamAttempt;
import com.learninge.app.data.model.ExamQuestion;
import com.learninge.app.domain.rules.ExamRules;
import com.learninge.app.domain.rules.LeitnerScheduler;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

/**
 * Nộp bài: chấm điểm và đưa từ của câu sai về hộp 1 trong một giao dịch (BR-054, BR-055).
 */
public final class ExamRepository {
    private final DatabaseHelper database;
    private final ExamDao exams;
    private final CardProgressDao cards;

    public ExamRepository(DatabaseHelper database) {
        this.database = database;
        exams = new ExamDao(database);
        cards = new CardProgressDao(database);
    }

    public <T> T inTransaction(Supplier<T> body) {
        return Tx.run(database, body);
    }

    /**
     * Trả số câu đúng.
     */
    public int submit(long userId, ExamAttempt attempt, LocalDate today, long now) {
        return Tx.run(database, () -> {
            List<ExamQuestion> questions = exams.listQuestions(attempt);
            int correct = 0;
            for (ExamQuestion question : questions) {
                if (question.isCorrect()) {
                    correct++;
                } else if (question.wordId() != null) {
                    cards.save(userId, question.wordId(), LeitnerScheduler.MIN_BOX,
                            LeitnerScheduler.nextReviewDate(LeitnerScheduler.MIN_BOX, today), today, now);
                }
            }
            exams.submit(attempt.id(), ExamRules.score(correct, questions.size()), correct, questions.size(),
                    ExamRules.usedSeconds(attempt.startedAt(), attempt.test().durationSec(), now), now);
            return correct;
        });
    }
}
