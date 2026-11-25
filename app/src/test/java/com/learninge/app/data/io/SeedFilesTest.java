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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Kiểm tra mọi file seed trong assets: đủ trường, không trùng, đủ số lượng tối thiểu đã chốt (C4, C5), đúng là
 * bản chép từ LearningE_Server.
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
                assertTrue(code + " words", deck.words().size() >= TOPIC_WORDS);
            }
            if (EXAM_WORDS.containsKey(code)) {
                assertTrue(code + " words", deck.words().size() >= EXAM_WORDS.get(code));
                assertFalse(code + " exam_tag", deck.examTag() == null);
            }
        }
    }

    @Test
    public void words_areValid() throws Exception {
        Set<String> uids = new HashSet<>();
        for (SeedDeck deck : decks()) {
            for (SeedWord word : deck.words()) {
                String uid = word.uid();
                // Không cần liên tục: từ bị xóa để lại khoảng trống vì uid không được dùng lại.
                assertTrue(uid, uid.matches(deck.code() + "-[0-9]{4}"));
                assertTrue("trùng uid " + uid, uids.add(uid));
                length(uid + " term", word.term(), 1, 50);
                length(uid + " meaning", word.meaning(), 1, 200);
                length(uid + " reading", word.reading(), 1, 60);
                length(uid + " pos", word.partOfSpeech(), 1, 20);
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

    /**
     * Sửa seed bằng tay thì lần chép sau mất, nên bản chép phải khớp {@code seed.lock}.
     */
    @Test
    public void files_matchSeedLock() throws Exception {
        Map<String, String> locked = new TreeMap<>();
        for (String line : read(new File("seed.lock")).split("\\R")) {
            if (line.startsWith("sha256 ")) {
                String[] parts = line.split(" ");
                locked.put(parts[1], parts[2]);
            }
        }
        Map<String, String> actual = new TreeMap<>();
        for (String root : List.of("main", "debug")) {
            Path folder = Path.of("src", root, "assets", "seed");
            if (!Files.exists(folder)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(folder)) {
                for (Path file : files.filter(Files::isRegularFile).collect(Collectors.toList())) {
                    byte[] text = read(file.toFile()).replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8);
                    StringBuilder hash = new StringBuilder();
                    for (byte b : MessageDigest.getInstance("SHA-256").digest(text)) {
                        hash.append(String.format("%02x", b));
                    }
                    actual.put(root + "/" + folder.relativize(file).toString().replace('\\', '/'), hash.toString());
                }
            }
        }
        assertEquals("seed khác seed.lock; chạy tools/sync-content.ps1 thay vì sửa tay", locked, actual);
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
        String text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        assertFalse(file + " có BOM", text.startsWith("\uFEFF"));
        return text;
    }
}
