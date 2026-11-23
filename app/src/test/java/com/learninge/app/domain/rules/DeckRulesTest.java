package com.learninge.app.domain.rules;

import static com.learninge.app.domain.rules.DeckRules.Field;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Set;

public class DeckRulesTest {

    @Test
    public void checkDeck_boundaries() {
        assertTrue(DeckRules.checkDeck("a", null).isEmpty());
        assertTrue(DeckRules.checkDeck("x".repeat(50), "y".repeat(200)).isEmpty());
        assertEquals(Set.of(Field.NAME), DeckRules.checkDeck("   ", ""));
        assertEquals(Set.of(Field.NAME, Field.DESCRIPTION), DeckRules.checkDeck("x".repeat(51), "y".repeat(201)));
    }

    @Test
    public void checkWord_termAndMeaningAreRequired() {
        assertTrue(DeckRules.checkWord(new WordInput("cat", null, "con mèo", "", null)).isEmpty());
        assertEquals(Set.of(Field.TERM, Field.MEANING),
                DeckRules.checkWord(new WordInput(" ", null, null, null, null)));
    }

    @Test
    public void checkWord_tooLong() {
        WordInput word = new WordInput("x".repeat(51), "x".repeat(61), "x".repeat(201), "x".repeat(301),
                "x".repeat(301));
        assertEquals(Set.of(Field.TERM, Field.READING, Field.MEANING, Field.EXAMPLE, Field.EXAMPLE_TRANSLATION),
                DeckRules.checkWord(word));
    }

    @Test
    public void clean_blankBecomesNull() {
        assertNull(DeckRules.clean("  "));
        assertNull(DeckRules.clean(null));
        assertEquals("a b", DeckRules.clean(" a b "));
    }
}
