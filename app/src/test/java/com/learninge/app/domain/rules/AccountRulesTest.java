package com.learninge.app.domain.rules;

import static com.learninge.app.domain.rules.AccountRules.Field;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Set;

public class AccountRulesTest {
    private static final RegisterForm VALID = form("a@learninge.test", "minh.ng", "Minh", "matkhau123", "matkhau123", 1,
            "Hà Nội");

    @Test
    public void email_boundaries() {
        assertTrue(AccountRules.isEmail("  Minh@Learninge.Test "));
        assertEquals("minh@learninge.test", AccountRules.normalizeEmail("  Minh@Learninge.Test "));
        for (String bad : new String[]{"", "minh", "minh@", "@a.b", "a@b", "a@@b.c", "a b@c.d", "a@b..c", "a@.b",
                "x".repeat(250) + "@a.vn"}) {
            assertFalse(bad, AccountRules.isEmail(bad));
        }
    }

    @Test
    public void nickname_boundaries() {
        assertTrue(AccountRules.isNickname("abc"));
        assertTrue(AccountRules.isNickname("a_b.1" + "x".repeat(15)));
        for (String bad : new String[]{"ab", "x".repeat(21), "có dấu", "a b c", "a@bc"}) {
            assertFalse(bad, AccountRules.isNickname(bad));
        }
    }

    @Test
    public void password_needsLetterAndDigit() {
        assertTrue(AccountRules.isPassword("abcdefg1"));
        assertTrue(AccountRules.isPassword("a1" + "x".repeat(62)));
        for (String bad : new String[]{"abc1234", "abcdefgh", "12345678", "a1" + "x".repeat(63)}) {
            assertFalse(bad, AccountRules.isPassword(bad));
        }
    }

    @Test
    public void checkRegister_valid_hasNoErrors() {
        assertTrue(AccountRules.checkRegister(VALID).isEmpty());
    }

    @Test
    public void checkRegister_reportsEveryWrongField() {
        RegisterForm bad = form("x", "x", "x", "x", "y", 0, "x");
        assertEquals(Set.of(Field.values()), AccountRules.checkRegister(bad));
        assertEquals(Set.of(Field.QUESTION), AccountRules.checkRegister(
                form("a@b.vn", "abc", "Minh", "abcdefg1", "abcdefg1", 6, "ok")));
    }

    @Test
    public void checkLogin_onlyNeedsEmailAndPassword() {
        assertTrue(AccountRules.checkLogin("a@b.vn", "x").isEmpty());
        assertEquals(Set.of(Field.EMAIL, Field.PASSWORD), AccountRules.checkLogin("a", ""));
    }

    @Test
    public void form_toString_hidesPassword() {
        assertFalse(VALID.toString().contains("matkhau123"));
    }

    private static RegisterForm form(String email, String nickname, String name, String password, String confirm,
                                     int question, String answer) {
        return new RegisterForm(email, nickname, name, password, confirm, question, answer);
    }
}
