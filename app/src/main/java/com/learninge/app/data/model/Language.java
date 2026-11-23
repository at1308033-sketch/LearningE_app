package com.learninge.app.data.model;

/**
 * Ngôn ngữ nội dung (BR-052): locale đọc và nhãn trường cách đọc.
 */
public record Language(String code, String name, String ttsLocale, String readingLabel) {
}
