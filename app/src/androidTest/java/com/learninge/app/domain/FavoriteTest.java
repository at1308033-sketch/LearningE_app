package com.learninge.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.learninge.app.TestApp;
import com.learninge.app.core.ErrorCode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FavoriteTest {
    private TestApp app;
    private DeckService service;

    @Before
    public void open() {
        app = new TestApp();
        service = app.services.deckService();
        app.deck(1, "T01", null, "A1");
        app.word(11, 1, "mother");
    }

    @After
    public void close() {
        app.close();
    }

    @Test
    public void toggleFavorite_twice_keepsTombstone() {
        assertTrue(service.toggleFavorite(11).value);
        assertTrue(service.getWord(11).value.item().isFavorite());
        assertFalse(service.toggleFavorite(11).value);
        assertFalse(service.getWord(11).value.item().isFavorite());
        assertEquals(1, app.number("SELECT COUNT(*) FROM favorites WHERE deleted = 1 AND dirty = 1"));
    }

    @Test
    public void getWord_hasDeckAndLanguage() {
        assertEquals("en-US", service.getWord(11).value.language().ttsLocale());
        assertEquals("T01", service.getWord(11).value.deck().code());
    }

    @Test
    public void getWord_ofOtherUser_isNotFound() {
        app.user(2);
        app.deck(5, "d-5", 2L, null);
        app.word(50, 5, "secret");
        assertEquals(ErrorCode.NOT_FOUND, service.getWord(50).error);
        assertEquals(ErrorCode.NOT_FOUND, service.toggleFavorite(50).error);
    }
}
