package com.learninge.app.domain.rules;

import java.util.EnumSet;
import java.util.Set;

/**
 * Độ dài và trường bắt buộc của bộ từ, từ (SRS §Validation, BR-044, BR-047). Java thuần.
 */
public final class DeckRules {
    public static final int MAX_NAME = 50;
    public static final int MAX_DESCRIPTION = 200;
    public static final int MAX_TERM = 50;
    public static final int MAX_READING = 60;
    public static final int MAX_MEANING = 200;
    public static final int MAX_EXAMPLE = 300;

    public enum Field {
        NAME, DESCRIPTION, TERM, READING, MEANING, EXAMPLE, EXAMPLE_TRANSLATION
    }

    private DeckRules() {
    }

    public static Set<Field> checkDeck(String name, String description) {
        Set<Field> errors = EnumSet.noneOf(Field.class);
        checkLength(errors, name, 1, MAX_NAME, Field.NAME);
        checkLength(errors, description, 0, MAX_DESCRIPTION, Field.DESCRIPTION);
        return errors;
    }

    public static Set<Field> checkWord(WordInput word) {
        Set<Field> errors = EnumSet.noneOf(Field.class);
        checkLength(errors, word.term(), 1, MAX_TERM, Field.TERM);
        checkLength(errors, word.reading(), 0, MAX_READING, Field.READING);
        checkLength(errors, word.meaning(), 1, MAX_MEANING, Field.MEANING);
        checkLength(errors, word.example(), 0, MAX_EXAMPLE, Field.EXAMPLE);
        checkLength(errors, word.exampleTranslation(), 0, MAX_EXAMPLE, Field.EXAMPLE_TRANSLATION);
        return errors;
    }

    /**
     * Chữ đã bỏ khoảng trắng hai đầu; rỗng thành null (cột tùy chọn lưu NULL).
     */
    public static String clean(String text) {
        String stripped = text == null ? "" : text.strip();
        return stripped.isEmpty() ? null : stripped;
    }

    private static void checkLength(Set<Field> errors, String text, int min, int max, Field field) {
        String cleaned = clean(text);
        int length = cleaned == null ? 0 : cleaned.codePointCount(0, cleaned.length());
        if (length < min || length > max) {
            errors.add(field);
        }
    }
}
