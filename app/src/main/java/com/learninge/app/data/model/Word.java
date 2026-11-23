package com.learninge.app.data.model;

/**
 * Một từ của bộ.
 */
public record Word(long id, String uuid, long deckId, String term, String reading, String partOfSpeech, String meaning,
                   String example, String exampleTranslation) {
}
