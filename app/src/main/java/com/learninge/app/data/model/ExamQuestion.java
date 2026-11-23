package com.learninge.app.data.model;

import java.util.List;

/**
 * Câu hỏi thi thử kèm câu trả lời hiện tại.
 *
 * @param wordId        từ gắn với câu, null nếu không có
 * @param selectedIndex lựa chọn của người dùng, null = chưa làm
 */
public record ExamQuestion(long id, int number, String passage, String question, List<String> options,
                           int correctIndex, String explanation, Long wordId, Integer selectedIndex,
                           boolean isFlagged) {

    public boolean isCorrect() {
        return selectedIndex != null && selectedIndex == correctIndex;
    }
}
