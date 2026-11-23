package com.learninge.app.data.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.learninge.app.data.io.SeedContent.SeedDeck;
import com.learninge.app.data.io.SeedContent.SeedLanguage;
import com.learninge.app.data.io.SeedContent.SeedWord;
import com.learninge.app.domain.rules.TextNormalizer;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Kiểm tra mọi file seed trong assets: đủ trường, không trùng, đúng số lượng đã chốt (C4, C5).
 */
public class SeedFilesTest {
    private static final File SEED = new File("src/main/assets/seed");
    private static final int TOPIC_WORDS = 20;
    private static final Map<String, Integer> EXAM_WORDS = Map.of("X01", 40, "X02", 30, "X03", 30);

    @Test
    public void languages_areTheFourSupported() throws Exception {
        List<String> codes = SeedParser.languages(read(new File(SEED, "languages.json"))).stream()
                .map(SeedLanguage::code).collect(Collectors.toList());
        assertEquals(List.of("en", "ja", "zh", "ko"), codes);
    }

    @Test
    public void decks_haveRequiredFields() throws Exception {
        for (SeedDeck deck : decks()) {
            String code = deck.code();
            assertTrue(code, code.matches("[TXJ][0-9]{2}"));
            assertTrue(code + " name", !deck.name().isBlank() && deck.name().length() <= 50);
            assertFalse(code + " source", deck.source().isBlank());
            assertTrue(code + " version", deck.version() >= 1);
            assertTrue(code + " language", Set.of("en", "ja", "zh", "ko").contains(deck.language()));
            if (code.startsWith("T")) {
                assertTrue(code + " level", Set.of("A1", "A2", "B1").contains(deck.level()));
                assertEquals(code + " words", TOPIC_WORDS, deck.words().size());
            }
            if (EXAM_WORDS.containsKey(code)) {
                assertEquals(code + " words", (int) EXAM_WORDS.get(code), deck.words().size());
                assertFalse(code + " exam_tag", deck.examTag() == null);
            }
        }
    }

    @Test
    public void words_areValid() throws Exception {
        for (SeedDeck deck : decks()) {
            int number = 0;
            for (SeedWord word : deck.words()) {
                String uid = word.uid();
                assertEquals("uid phải liên tục", String.format("%s-%04d", deck.code(), ++number), uid);
                length(uid + " term", word.term(), 1, 50);
                length(uid + " meaning", word.meaning(), 1, 200);
                length(uid + " reading", word.reading(), 1, 60);
                length(uid + " pos", word.partOfSpeech(), 1, 30);
                length(uid + " example", word.example(), 1, 300);
                length(uid + " example_translation", word.exampleTranslation(), 1, 300);
                if (deck.language().equals("en")) {
                    assertTrue(uid + " IPA nằm trong //", word.reading().matches("/.+/"));
                }
            }
        }
    }

    @Test
    public void words_haveNoDuplicateTermInDeck() throws Exception {
        for (SeedDeck deck : decks()) {
            Set<String> seen = new HashSet<>();
            for (SeedWord word : deck.words()) {
                assertTrue(word.uid() + " trùng " + word.term(),
                        seen.add(TextNormalizer.searchKey(word.term(), deck.language())));
            }
        }
    }

    private static void length(String what, String text, int min, int max) {
        assertTrue(what + " rỗng", text != null && text.strip().equals(text) && text.length() >= min);
        assertTrue(what + " quá " + max + " ký tự", text.length() <= max);
    }

    private static List<SeedDeck> decks() throws Exception {
        List<SeedDeck> decks = new ArrayList<>();
        File[] files = new File(SEED, "decks").listFiles((dir, name) -> name.endsWith(".json"));
        for (File file : files == null ? new File[0] : files) {
            SeedDeck deck = SeedParser.deck(read(file));
            assertEquals("tên tệp = mã bộ", deck.code() + ".json", file.getName());
            decks.add(deck);
        }
        return decks;
    }

    private static String read(File file) throws Exception {
        String text = new String(Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        assertFalse(file + " có BOM", text.startsWith("﻿"));
        return text;
    }
}
