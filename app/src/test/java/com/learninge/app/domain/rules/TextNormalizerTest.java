package com.learninge.app.domain.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class TextNormalizerTest {

    @Test
    public void normalize_ignoresCaseAndExtraSpacesButKeepsMarks() {
        assertEquals("apple pie", TextNormalizer.normalize("  Apple   PIE "));
        assertNotEquals(TextNormalizer.normalize("ma"), TextNormalizer.normalize("má"));
    }

    @Test
    public void stripMarks_removesVietnameseMarks() {
        assertEquals("ha noi", TextNormalizer.stripMarks(" Hà Nội "));
        assertEquals("da nang", TextNormalizer.stripMarks("ĐÀ NẴNG"));
        assertEquals("con meo", TextNormalizer.stripMarks("con mèo"));
    }

    @Test
    public void searchKey_keepsCjkCharactersDistinct() {
        // が = か + dấu đục: bỏ dấu sẽ thành chữ khác.
        assertNotEquals(TextNormalizer.searchKey("が", "ja"), TextNormalizer.searchKey("か", "ja"));
        assertEquals("cafe", TextNormalizer.searchKey("Café", "en"));
        assertEquals("你好", TextNormalizer.searchKey(" 你好 ", "zh"));
        assertEquals(TextNormalizer.searchKey("한국", "ko"), TextNormalizer.searchKey(" 한국", "ko"));
    }
}
