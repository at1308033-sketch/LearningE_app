package com.learninge.app.ui.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.domain.rules.AccountRules.Field;

import org.junit.Test;

import java.util.Set;

public class AuthFormStateTest {

    @Test
    public void failed_validation_readsFieldsFromAppAndServer() {
        assertEquals(Set.of(Field.EMAIL, Field.DISPLAY_NAME),
                failed(ErrorCode.VALIDATION, "EMAIL,DISPLAY_NAME").fieldErrors());
        AuthFormState state = failed(ErrorCode.VALIDATION, "password=TOO_SHORT,security_answer=REQUIRED");
        assertEquals(Set.of(Field.PASSWORD, Field.ANSWER), state.fieldErrors());
        assertNull(state.error());
    }

    @Test
    public void failed_validation_unknownField_isGeneralError() {
        assertEquals(ErrorCode.VALIDATION, failed(ErrorCode.VALIDATION, "device_name=TOO_LONG").error());
    }

    @Test
    public void failed_taken_marksField() {
        assertEquals(Set.of(Field.EMAIL), failed(ErrorCode.EMAIL_TAKEN, null).fieldErrors());
        assertEquals(Set.of(Field.NICKNAME), failed(ErrorCode.NICKNAME_TAKEN, null).fieldErrors());
    }

    @Test
    public void failed_locked_countsFromNow() {
        assertEquals(61_000, failed(ErrorCode.ACCOUNT_LOCKED, "60").lockedUntil());
        assertEquals(0, failed(ErrorCode.INVALID_CREDENTIALS, null).lockedUntil());
    }

    private static AuthFormState failed(ErrorCode code, String detail) {
        return AuthFormState.failed(Result.error(code, detail), 1_000);
    }
}
