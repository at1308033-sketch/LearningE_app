package com.learninge.app.domain.rules;

/**
 * Nội dung người dùng nhập ở SCR-20; trường tùy chọn có thể null.
 */
public record WordInput(String term, String reading, String meaning, String example, String exampleTranslation) {
}
