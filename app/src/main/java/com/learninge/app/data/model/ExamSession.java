package com.learninge.app.data.model;

import java.util.List;

/**
 * Lượt thi và các câu hỏi, dùng cho màn làm bài và màn kết quả.
 */
public record ExamSession(ExamAttempt attempt, List<ExamQuestion> questions) {

    public int answered() {
        return (int) questions.stream().filter(question -> question.selectedIndex() != null).count();
    }

    public int flagged() {
        return (int) questions.stream().filter(ExamQuestion::isFlagged).count();
    }
}
