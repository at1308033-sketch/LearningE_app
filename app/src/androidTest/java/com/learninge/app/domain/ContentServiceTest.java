package com.learninge.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.learninge.app.TestApp;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.io.SeedContent.SeedDeck;
import com.learninge.app.data.io.SeedContent.SeedExam;
import com.learninge.app.data.io.SeedContent.SeedQuestion;
import com.learninge.app.data.io.SeedContent.SeedWord;
import com.learninge.app.data.remote.ContentRemote;
import com.learninge.app.data.repo.ContentRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(AndroidJUnit4.class)
public class ContentServiceTest {
    private TestApp app;
    private FakeRemote remote;
    private ContentService service;

    @Before
    public void open() {
        app = new TestApp();
        remote = new FakeRemote();
        service = new ContentService(() -> remote, new ContentRepository(app.services.database(),
                app.services.clock()), app.services.executors());
        app.deck(1, "T01", null, "A1");
        app.word(11, 1, "mother");
        app.sql("UPDATE words SET uuid = 'T01-0001' WHERE id = 11");
        app.sql("INSERT INTO content_version VALUES ('deck:T01', 1, 0)");
        app.progress(11, 3, "2026-11-14");
    }

    @After
    public void close() {
        app.close();
    }

    @Test
    public void update_installsNewerItemsAndKeepsProgress() {
        assertEquals(2, (int) service.update().value);
        assertEquals("mum", app.text("SELECT term FROM words WHERE id = 11"));
        assertEquals(3, app.number("SELECT box FROM card_progress WHERE word_id = 11"));
        assertEquals(11, app.number("SELECT word_id FROM exam_questions"));
        assertEquals(9, app.number("SELECT version FROM content_version WHERE item_key = 'global'"));
    }

    @Test
    public void update_notModified_downloadsNothing() {
        service.update();
        remote.downloads = 0;
        remote.isNotModified = true;
        assertEquals(0, (int) service.update().value);
        assertEquals(0, remote.downloads);
        assertEquals(9, remote.askedVersion);
    }

    @Test
    public void update_failureHalfway_changesNothing() {
        remote.examError = ErrorCode.SERVER_UNAVAILABLE;
        assertFalse(service.update().isOk());
        assertEquals("mother", app.text("SELECT term FROM words WHERE id = 11"));
        assertEquals(-1, app.number("SELECT version FROM content_version WHERE item_key = 'global'"));
    }

    @Test
    public void update_itemAlreadyInstalled_isSkipped() {
        app.sql("UPDATE content_version SET version = 2 WHERE item_key = 'deck:T01'");
        assertEquals(1, (int) service.update().value);
        assertEquals("mother", app.text("SELECT term FROM words WHERE id = 11"));
    }

    @Test
    public void update_retiredDeck_isHiddenAndComesBackWithProgress() {
        remote.isRetired = true;
        service.update();
        assertEquals(1, app.number("SELECT deleted FROM decks WHERE code = 'T01'"));
        assertEquals(1, app.number("SELECT deleted FROM words WHERE id = 11"));
        assertEquals(3, app.number("SELECT box FROM card_progress WHERE word_id = 11"));
        assertTrue(app.services.deckService().listTopics(null).value.isEmpty());

        remote.isRetired = false;
        remote.version = 3;
        service.update();
        assertEquals(0, app.number("SELECT deleted FROM words WHERE id = 11"));
        assertEquals(1, app.services.deckService().listTopics(null).value.size());
    }

    @Test
    public void update_retiredExam_isListedOnlyWhileAnAttemptIsOpen() {
        service.update();
        assertTrue(app.services.examService().startOrResume("E01").isOk());
        remote.isRetired = true;
        remote.version = 3;
        service.update();
        assertEquals(1, app.services.examService().listTests().value.size());
        app.sql("UPDATE exam_attempts SET status = 'SUBMITTED', submitted_at = 1, score = 0, correct_count = 0, "
                + "duration_sec = 1");
        assertTrue(app.services.examService().listTests().value.isEmpty());
        assertEquals(1, app.services.examService().history().value.size());
    }

    @Test
    public void update_examQuestionRemovedInTheMiddle_keepsTheOthers() {
        remote.numbers = List.of(1, 2, 3);
        service.update();
        remote.numbers = List.of(1, 3);
        remote.version = 3;
        service.update();
        assertEquals("1,3", app.text("SELECT group_concat(number) FROM (SELECT number FROM exam_questions "
                + "ORDER BY number)"));
    }

    @Test
    public void update_manyDecks_downloadsTenAtATime() {
        remote.deckCodes = IntStream.rangeClosed(1, 23).mapToObj(n -> String.format("T%02d", n))
                .collect(Collectors.toList());
        assertEquals(24, (int) service.update().value);
        assertEquals(List.of(10, 10, 3), remote.askedDecks.stream().map(List::size).collect(Collectors.toList()));
        assertEquals(23, app.number("SELECT COUNT(*) FROM decks WHERE code LIKE 'T%'"));
    }

    private static final class FakeRemote extends ContentRemote {
        private boolean isNotModified;
        private boolean isRetired;
        private int version = 2;
        private List<Integer> numbers = List.of(1);
        private List<String> deckCodes = List.of("T01");
        private final List<List<String>> askedDecks = new ArrayList<>();
        private ErrorCode examError;
        private int downloads;
        private long askedVersion;

        private FakeRemote() {
            super(null);
        }

        @Override
        public Result<Manifest> manifest(long installedVersion) {
            askedVersion = installedVersion;
            return Result.ok(new Manifest(9, isNotModified, List.of(),
                    deckCodes.stream().map(code -> new Item(code, version)).collect(Collectors.toList()),
                    List.of(new Item("E01", version))));
        }

        @Override
        public Result<List<SeedDeck>> decks(List<String> codes) {
            downloads++;
            askedDecks.add(codes);
            return Result.ok(codes.stream().map(code -> new SeedDeck(code, version, "Gia đình", null, "en", "A1", null,
                    null, 1, "server", List.of(new SeedWord(code + "-0001", "mum", null, null, "mẹ", null, null)),
                    isRetired)).collect(Collectors.toList()));
        }

        @Override
        public Result<List<SeedExam>> exams(List<String> codes) {
            downloads++;
            List<SeedQuestion> questions = new ArrayList<>();
            for (int number : numbers) {
                questions.add(new SeedQuestion(number, null, "?", "[\"a\",\"b\",\"c\",\"d\"]", 0, null, null, null,
                        "T01-0001"));
            }
            return examError != null ? Result.error(examError) : Result.ok(codes.stream()
                    .map(code -> new SeedExam(code, version, "TOEIC", "Đề 1", null, 900, null, 1, questions, isRetired))
                    .collect(Collectors.toList()));
        }
    }
}
