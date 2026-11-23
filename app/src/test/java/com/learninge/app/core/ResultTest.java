package com.learninge.app.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ResultTest {

    @Test
    public void ok_hasValueAndNoError() {
        Result<String> result = Result.ok("x");
        assertTrue(result.isOk());
        assertEquals("x", result.value);
        assertNull(result.error);
    }

    @Test
    public void errorOf_keepsCodeAndDetail() {
        Result<Integer> result = Result.errorOf(Result.<String>error(ErrorCode.ACCOUNT_LOCKED, "42"));
        assertFalse(result.isOk());
        assertEquals(ErrorCode.ACCOUNT_LOCKED, result.error);
        assertEquals(42, result.detailAsInt(0));
    }

    @Test
    public void detailAsInt_fallsBackWhenNotANumber() {
        assertEquals(7, Result.error(ErrorCode.DB).detailAsInt(7));
        assertEquals(7, Result.error(ErrorCode.DB, "abc").detailAsInt(7));
    }
}
