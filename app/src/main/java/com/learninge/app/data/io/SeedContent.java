package com.learninge.app.data.io;

import java.util.List;

/**
 * Nội dung đọc từ file seed, chưa ghi vào CSDL.
 */
public final class SeedContent {

    public record SeedLanguage(String code, String nameVi, String ttsLocale, String readingLabel, int sortOrder) {
    }

    public record SeedWord(String uid, String term, String reading, String partOfSpeech, String meaning,
                           String example, String exampleTranslation) {
    }

    public record SeedDeck(String code, int version, String name, String description, String language, String level,
                           String icon, String examTag, int sortOrder, String source, List<SeedWord> words) {
    }

    public record SeedQuestion(int number, String passage, String question, String optionsJson, int correctIndex,
                               String explanation, String deckCode, String term) {
    }

    public record SeedExam(String code, int version, String examTag, String title, String partLabel, int durationSec,
                           String description, int sortOrder, List<SeedQuestion> questions) {
    }

    private SeedContent() {
    }
}
