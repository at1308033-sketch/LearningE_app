package com.learninge.app.data.model;

/**
 * XP vừa nhận.
 *
 * @param isCapped true nếu đã chạm trần 500 XP/ngày
 */
public record XpGain(int xp, boolean isCapped) {
    public static final XpGain NONE = new XpGain(0, false);
}
