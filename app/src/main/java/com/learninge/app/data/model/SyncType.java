package com.learninge.app.data.model;

/**
 * Loại bản ghi đồng bộ, xếp theo thứ tự cha trước con để server luôn thấy bộ trước từ, từ trước tiến độ.
 */
public enum SyncType {
    SETTINGS, DECK, WORD, CARD_PROGRESS, FAVORITE, STUDY_LOG, QUIZ_ATTEMPT, EXAM_ATTEMPT, XP_EVENT
}
