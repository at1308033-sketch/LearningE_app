package com.learninge.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.learninge.app.TestApp;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.data.model.WordItem;
import com.learninge.app.domain.rules.LeitnerScheduler.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DeckServiceTest {
    private TestApp app;
    private DeckService service;

    @Before
    public void open() {
        app = new TestApp();
        service = app.services.deckService();
        app.deck(1, "T01", null, "A1");
        app.deck(2, "T08", null, "A2");
        app.deck(3, "X01", null, "B1");
        app.word(11, 1, "mother");
        app.word(12, 1, "father");
        app.word(13, 1, "sister");
        app.progress(11, 5, "2026-11-20");
        app.progress(12, 2, "2026-11-10");
    }

    @After
    public void close() {
        app.close();
    }

    @Test
    public void listTopics_filtersByLevelAndHidesExamDecks() {
        assertEquals(2, service.listTopics(null).value.size());
        List<DeckSummary> a1 = service.listTopics("A1").value;
        assertEquals(1, a1.size());
        assertEquals("T01", a1.get(0).deck().code());
    }

    @Test
    public void listTopics_countsByStatusAndDue() {
        DeckSummary deck = service.listTopics("A1").value.get(0);
        assertEquals(3, deck.total());
        assertEquals(1, deck.mastered());
        assertEquals(1, deck.learning());
        assertEquals(1, deck.newWords());
        assertEquals(1, deck.due());
    }

    @Test
    public void listWords_hasStatusOfCurrentUserOnly() {
        app.user(2);
        app.sql("INSERT INTO card_progress (user_id, word_id, box, next_review_date, first_learned_date, "
                + "last_reviewed_at, updated_at) VALUES (2, 13, 5, '2026-11-20', '2026-11-10', 0, 0)");
        List<WordItem> words = service.listWords(1).value;
        assertEquals(Status.MASTERED, words.get(0).status());
        assertEquals(Status.LEARNING, words.get(1).status());
        assertEquals(Status.NEW, words.get(2).status());
        assertNull(words.get(2).box());
    }

    @Test
    public void getDeck_ofOtherUser_isNotFound() {
        app.user(2);
        app.deck(9, "d-9", 2L, null);
        assertEquals(ErrorCode.NOT_FOUND, service.getDeck(9).error);
        assertTrue(service.getDeck(1).isOk());
    }
}
