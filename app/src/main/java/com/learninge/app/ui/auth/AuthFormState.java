package com.learninge.app.ui.auth;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.domain.rules.AccountRules.Field;

import java.util.EnumSet;
import java.util.Set;

/**
 * Trạng thái form đăng nhập/đăng ký.
 *
 * @param error         lỗi chung của form, null nếu không có
 * @param fieldErrors   các trường sai
 * @param lockedUntil   mốc {@code SystemClock.elapsedRealtime()} hết khóa tạm, 0 nếu không bị khóa
 */
record AuthFormState(boolean isLoading, boolean isDone, ErrorCode error, Set<Field> fieldErrors, long lockedUntil) {
    static final AuthFormState IDLE = new AuthFormState(false, false, null, Set.of(), 0);
    static final AuthFormState LOADING = new AuthFormState(true, false, null, Set.of(), 0);
    static final AuthFormState DONE = new AuthFormState(false, true, null, Set.of(), 0);

    static AuthFormState failed(Result<?> result, long elapsedNow) {
        Set<Field> fields = EnumSet.noneOf(Field.class);
        long lockedUntil = 0;
        ErrorCode general = result.error;
        switch (result.error) {
            case VALIDATION:
                fields.addAll(fieldsOf(result.detail));
                general = fields.isEmpty() ? ErrorCode.VALIDATION : null;
                break;
            case EMAIL_TAKEN:
                fields.add(Field.EMAIL);
                break;
            case NICKNAME_TAKEN:
                fields.add(Field.NICKNAME);
                break;
            case ACCOUNT_LOCKED:
            case RATE_LIMITED:
                lockedUntil = elapsedNow + 1_000L * Math.max(1, result.detailAsInt(60));
                break;
            default:
                break;
        }
        return new AuthFormState(false, false, general, fields, lockedUntil);
    }

    // "EMAIL,PASSWORD" từ app hoặc "email=INVALID_FORMAT,password=…" từ máy chủ.
    private static Set<Field> fieldsOf(String detail) {
        Set<Field> fields = EnumSet.noneOf(Field.class);
        for (String part : detail == null ? new String[0] : detail.split(",")) {
            String name = part.split("=")[0].trim().toLowerCase(java.util.Locale.ROOT);
            switch (name) {
                case "email":
                    fields.add(Field.EMAIL);
                    break;
                case "nickname":
                    fields.add(Field.NICKNAME);
                    break;
                case "display_name":
                    fields.add(Field.DISPLAY_NAME);
                    break;
                case "password":
                    fields.add(Field.PASSWORD);
                    break;
                case "confirm":
                    fields.add(Field.CONFIRM);
                    break;
                case "question":
                case "security_question_id":
                    fields.add(Field.QUESTION);
                    break;
                case "answer":
                case "security_answer":
                    fields.add(Field.ANSWER);
                    break;
                default:
                    break;
            }
        }
        return fields;
    }
}
