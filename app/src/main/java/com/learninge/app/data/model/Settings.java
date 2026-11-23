package com.learninge.app.data.model;

/**
 * Cài đặt học của người dùng. Nhắc học là cài đặt riêng từng máy, không đồng bộ (BR-030).
 */
public record Settings(String level, int dailyGoal, boolean isReminderEnabled, int reminderMinute) {
}
