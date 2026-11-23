package com.learninge.app.domain.rules;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Kiểm tra định dạng tài khoản trước khi gửi (SRS §Validation, BR-001, BR-009, BR-060, BR-061). Server kiểm lại.
 */
public final class AccountRules {
    private static final int MAX_EMAIL = 254;
    private static final int QUESTION_COUNT = 5;
    // Đúng một '@', không khoảng trắng, tên miền có dấu chấm nằm giữa.
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+");
    private static final Pattern NICKNAME = Pattern.compile("[A-Za-z0-9_.]{3,20}");
    private static final Pattern LETTER = Pattern.compile("\\p{L}");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");

    public enum Field {
        EMAIL, NICKNAME, DISPLAY_NAME, PASSWORD, CONFIRM, QUESTION, ANSWER
    }

    private AccountRules() {
    }

    public static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    public static boolean isEmail(String email) {
        String normalized = normalizeEmail(email);
        return normalized.length() <= MAX_EMAIL && EMAIL.matcher(normalized).matches();
    }

    public static boolean isNickname(String nickname) {
        return NICKNAME.matcher(nickname).matches();
    }

    public static boolean isDisplayName(String name) {
        return hasLength(name.strip(), 2, 30);
    }

    public static boolean isPassword(String password) {
        return hasLength(password, 8, 64) && LETTER.matcher(password).find() && DIGIT.matcher(password).find();
    }

    public static boolean isAnswer(String answer) {
        return hasLength(answer.strip(), 2, 50);
    }

    public static Set<Field> checkRegister(RegisterForm form) {
        Set<Field> errors = EnumSet.noneOf(Field.class);
        check(errors, isEmail(form.email()), Field.EMAIL);
        check(errors, isNickname(form.nickname()), Field.NICKNAME);
        check(errors, isDisplayName(form.displayName()), Field.DISPLAY_NAME);
        check(errors, isPassword(form.password()), Field.PASSWORD);
        check(errors, form.password().equals(form.confirm()), Field.CONFIRM);
        check(errors, form.questionId() >= 1 && form.questionId() <= QUESTION_COUNT, Field.QUESTION);
        check(errors, isAnswer(form.answer()), Field.ANSWER);
        return errors;
    }

    /**
     * Đăng nhập chỉ kiểm trường trống và email, để không lộ quy tắc mật khẩu cũ.
     */
    public static Set<Field> checkLogin(String email, String password) {
        Set<Field> errors = EnumSet.noneOf(Field.class);
        check(errors, isEmail(email), Field.EMAIL);
        check(errors, !password.isEmpty(), Field.PASSWORD);
        return errors;
    }

    private static void check(Set<Field> errors, boolean isValid, Field field) {
        if (!isValid) {
            errors.add(field);
        }
    }

    private static boolean hasLength(String text, int min, int max) {
        int length = text.codePointCount(0, text.length());
        return length >= min && length <= max;
    }
}
