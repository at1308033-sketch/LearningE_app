package com.learninge.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.learninge.app.TestApp;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.domain.rules.WordInput;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DeckEditorTest {
    private TestApp app;
    private DeckEditor editor;
    private DeckService service;
    private long deckId;

    @Before
    public void open() {
        app = new TestApp();
        editor = app.services.deckEditor();
        service = app.services.deckService();
        app.deck(1, "T01", null, "A1");
        app.deck(2, "X01", null, "B1");
        app.word(11, 1, "mother");
        deckId = editor.createDeck(" Công việc ", "", "en").value;
    }

    @After
    public void close() {
        app.close();
    }

    @Test
    public void createDeck_isDirtyWithUuid() {
        assertEquals("Công việc", app.text("SELECT name FROM decks WHERE id = " + deckId));
        assertEquals(36, app.text("SELECT uuid FROM decks WHERE id = " + deckId).length());
        assertEquals(1, app.number("SELECT dirty FROM decks WHERE id = " + deckId));
    }

    @Test
    public void createDeck_sameNameIgnoringCase_isDuplicate() {
        assertEquals(ErrorCode.DUPLICATE, editor.createDeck("CÔNG  việc", null, "en").error);
        assertEquals(ErrorCode.VALIDATION, editor.createDeck(" ", null, "en").error);
    }

    @Test
    public void updateDeck_keepsOwnNameAndLocksLanguageWhenNotEmpty() {
        assertTrue(editor.updateDeck(deckId, "Công việc", "mô tả", "ja").isOk());
        editor.addWord(deckId, word("猫"));
        assertEquals(ErrorCode.READ_ONLY, editor.updateDeck(deckId, "Công việc", null, "en").error);
        assertTrue(editor.updateDeck(deckId, "Tên mới", null, "ja").isOk());
    }

    @Test
    public void builtinDeck_isReadOnly() {
        assertEquals(ErrorCode.READ_ONLY, editor.updateDeck(1, "x", null, "en").error);
        assertEquals(ErrorCode.READ_ONLY, editor.deleteDeck(1).error);
        assertEquals(ErrorCode.READ_ONLY, editor.addWord(1, word("new")).error);
        assertEquals(ErrorCode.READ_ONLY, editor.deleteWord(11).error);
    }

    @Test
    public void defaultDeck_cannotBeDeleted() {
        app.sql("UPDATE decks SET is_default = 1 WHERE id = " + deckId);
        assertEquals(ErrorCode.READ_ONLY, editor.deleteDeck(deckId).error);
    }

    @Test
    public void deleteDeck_marksDeckAndWords() {
        editor.addWord(deckId, word("cat"));
        assertTrue(editor.deleteDeck(deckId).isOk());
        assertEquals(1, app.number("SELECT deleted + dirty - 1 FROM decks WHERE id = " + deckId));
        assertEquals(0, app.number("SELECT COUNT(*) FROM words WHERE deck_id = " + deckId + " AND deleted = 0"));
        // Xóa rồi thì tạo lại cùng tên được.
        assertTrue(editor.createDeck("Công việc", null, "en").isOk());
    }

    @Test
    public void addWord_duplicateTerm_rejectedUntilDeleted() {
        long wordId = editor.addWord(deckId, word("Cat")).value;
        assertEquals(ErrorCode.DUPLICATE, editor.addWord(deckId, word(" cat ")).error);
        assertTrue(editor.updateWord(wordId, word("CAT")).isOk());
        assertTrue(editor.deleteWord(wordId).isOk());
        assertTrue(editor.addWord(deckId, word("cat")).isOk());
    }

    @Test
    public void copyToDeck_makesNewWord() {
        long copy = editor.copyToDeck(11, deckId).value;
        assertEquals(36, app.text("SELECT uuid FROM words WHERE id = " + copy).length());
        assertEquals(ErrorCode.DUPLICATE, editor.copyToDeck(11, deckId).error);
        assertEquals(ErrorCode.READ_ONLY, editor.copyToDeck(11, 2).error);
    }

    @Test
    public void listMyDecks_hasFavoritesFirstAndHidesTopics() {
        service.toggleFavorite(11);
        List<DeckSummary> decks = service.listMyDecks().value;
        assertEquals(DeckService.FAVORITES, decks.get(0).deck().id());
        assertEquals(1, decks.get(0).total());
        assertEquals(deckId, decks.get(1).deck().id());
        assertEquals("X01", decks.get(2).deck().code());
        assertEquals(3, decks.size());
    }

    @Test
    public void favorites_canBeStudied() {
        service.toggleFavorite(11);
        assertEquals(1, service.listWords(DeckService.FAVORITES).value.size());
        assertEquals(1, app.services.studyService().newSession(DeckService.FAVORITES).value.size());
        assertEquals(ErrorCode.READ_ONLY, editor.deleteDeck(DeckService.FAVORITES).error);
    }

    private static WordInput word(String term) {
        return new WordInput(term, null, "nghĩa", null, null);
    }
}
