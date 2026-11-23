package com.learninge.app.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.learninge.app.TestApp;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.SyncRecord;
import com.learninge.app.data.model.SyncType;
import com.learninge.app.data.remote.PullPage;
import com.learninge.app.data.remote.PushResult;
import com.learninge.app.data.remote.SyncRemote;
import com.learninge.app.data.repo.SyncRepository;
import com.learninge.app.domain.rules.WordInput;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RunWith(AndroidJUnit4.class)
public class SyncManagerTest {
    private TestApp app;
    private FakeRemote remote;
    private SyncManager manager;

    @Before
    public void open() {
        app = new TestApp();
        remote = new FakeRemote();
        manager = new SyncManager(() -> remote, new SyncRepository(app.services.database()),
                app.services.sessionStore(), app.services.executors(), app.services.clock());
        app.deck(1, "T01", null, "A1");
        app.word(11, 1, "mother");
        app.sql("UPDATE sync_state SET pull_cursor = 7");
    }

    @After
    public void close() {
        app.close();
    }

    @Test
    public void push_sendsParentsFirstAndClearsDirty() {
        long deckId = app.services.deckEditor().createDeck("Bộ", null, "en").value;
        app.services.deckEditor().addWord(deckId, new WordInput("cat", null, "mèo", null, null));
        app.services.studyService().rate(11, true);
        app.services.deckService().toggleFavorite(11);
        assertTrue(manager.syncOnce().isOk());
        assertEquals(List.of(SyncType.DECK, SyncType.WORD, SyncType.CARD_PROGRESS, SyncType.FAVORITE),
                remote.pushed.stream().map(SyncRecord::type).collect(Collectors.toList()));
        assertEquals("w-11", remote.pushed.get(2).key());
        assertEquals(0, app.services.syncRepository().countPending(1));
        assertNotNull(app.services.syncRepository().state(1).lastSyncAt());
    }

    @Test
    public void push_recordEditedMeanwhile_staysDirty() {
        app.services.studyService().rate(11, true);
        remote.beforeReply = () -> app.sql("UPDATE card_progress SET updated_at = updated_at + 1");
        manager.syncOnce();
        assertEquals(1, app.services.syncRepository().countPending(1));
    }

    @Test
    public void push_stale_takesServerVersion() {
        app.services.studyService().rate(11, true);
        remote.reply = sent -> new PushResult(PushResult.Status.STALE, 9, card("w-11", 1, sent.updatedAt() + 5), null);
        manager.syncOnce();
        assertEquals(1, app.number("SELECT box FROM card_progress"));
        assertEquals(0, app.services.syncRepository().countPending(1));
    }

    @Test
    public void push_rejected_isNotRetriedButReported() {
        app.services.studyService().rate(11, true);
        remote.reply = sent -> new PushResult(PushResult.Status.REJECTED, 0, null, "INVALID_RECORD");
        assertTrue(manager.syncOnce().isOk());
        assertEquals(0, app.services.syncRepository().countPending(1));
        assertEquals("INVALID_RECORD", app.services.syncRepository().state(1).lastErrorCode());
    }

    @Test
    public void push_networkError_keepsEverything() {
        app.services.studyService().rate(11, true);
        remote.error = ErrorCode.NETWORK_OFFLINE;
        assertFalse(manager.syncOnce().isOk());
        assertEquals(1, app.services.syncRepository().countPending(1));
        assertEquals("NETWORK_OFFLINE", app.services.syncRepository().state(1).lastErrorCode());
        assertNull(app.services.syncRepository().state(1).lastSyncAt());
    }

    @Test
    public void push_moreThan500_usesBatches() {
        for (int i = 0; i < 620; i++) {
            app.sql("INSERT INTO study_log (uuid, user_id, activity_type, study_date, created_at, updated_at) "
                    + "VALUES ('s" + i + "', 1, 'QUIZ', '2026-11-09', 0, " + i + ")");
        }
        assertTrue(manager.syncOnce().isOk());
        assertEquals(List.of(500, 120), remote.batchSizes);
    }

    @Test
    public void firstSync_pullsBeforePush() {
        app.sql("UPDATE sync_state SET pull_cursor = 0");
        app.services.studyService().rate(11, true);
        manager.syncOnce();
        assertEquals("pull", remote.calls.get(0));
        assertEquals("push", remote.calls.get(1));
    }

    @Test
    public void firstSync_pushesNotebookOnlyWhenServerLacksIt() {
        app.sql("UPDATE sync_state SET pull_cursor = 0");
        app.sql("INSERT INTO decks (id, uuid, owner_user_id, name, name_norm, language_code, is_default, created_at, "
                + "updated_at, dirty) VALUES (5, 'note', 1, 'Sổ tay', 'sổ tay', 'en', 1, 0, 0, 0)");
        remote.pages.add(new PullPage(List.of(deck("note", "Tên mới trên máy khác")), 3, false));
        manager.syncOnce();
        assertTrue(remote.pushed.isEmpty());
        assertEquals("Tên mới trên máy khác", app.text("SELECT name FROM decks WHERE id = 5"));
    }

    @Test
    public void pull_childBeforeParent_isAppliedLater() {
        remote.pages.add(new PullPage(List.of(card("w-new", 3, 100)), 8, true));
        remote.pages.add(new PullPage(List.of(word("w-new", "d-new", "dog", 100), deck("d-new", "Bộ máy khác")), 10,
                false));
        manager.syncOnce();
        assertEquals(3, app.number("SELECT p.box FROM card_progress p JOIN words w ON w.id = p.word_id "
                + "WHERE w.uuid = 'w-new'"));
        assertEquals(10, app.services.syncRepository().state(1).cursor());
    }

    @Test
    public void pull_olderThanLocalEdit_keepsLocal() {
        app.services.studyService().rate(11, true);
        remote.error = ErrorCode.NETWORK_OFFLINE;
        manager.syncOnce();
        new SyncRepository(app.services.database()).applyPull(1, new PullPage(List.of(card("w-11", 5, 1)), 9, false),
                new ArrayList<>());
        assertEquals(2, app.number("SELECT box FROM card_progress"));
    }

    @Test
    public void pull_sameDeckName_isRenamedAndQueued() {
        app.services.deckEditor().createDeck("TOEIC", null, "en");
        manager.syncOnce();
        remote.pages.add(new PullPage(List.of(deck("d-other", "toeic")), 20, false));
        manager.syncOnce();
        assertEquals("toeic (2)", app.text("SELECT name FROM decks WHERE uuid = 'd-other'"));
        assertEquals(1, app.number("SELECT dirty FROM decks WHERE uuid = 'd-other'"));
    }

    @Test
    public void pull_sameWordOnTwoDevices_keepsNewer() {
        long deckId = app.services.deckEditor().createDeck("Bộ", null, "en").value;
        String deckUuid = app.text("SELECT uuid FROM decks WHERE id = " + deckId);
        app.services.deckEditor().addWord(deckId, new WordInput("cat", null, "mèo", null, null));
        manager.syncOnce();
        long newer = app.services.clock().millis() + 10;
        remote.pages.add(new PullPage(List.of(word("w-other", deckUuid, "Cat", newer)), 20, false));
        manager.syncOnce();
        assertEquals("w-other", app.text("SELECT uuid FROM words WHERE deck_id = " + deckId + " AND deleted = 0"));
        assertEquals(1, app.number("SELECT COUNT(*) FROM words WHERE deck_id = " + deckId
                + " AND deleted = 1 AND dirty = 1"));
    }

    @Test
    public void pull_studyLogFromOtherDevice_updatesStreak() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("activity_type", "LEARN");
        fields.put("study_date", "2026-11-09");
        fields.put("created_at", 1L);
        remote.pages.add(new PullPage(List.of(new SyncRecord(SyncType.STUDY_LOG, "s-1", 1, false, 8, fields),
                new SyncRecord(SyncType.STUDY_LOG, "s-1", 1, false, 8, fields)), 8, false));
        manager.syncOnce();
        assertEquals(1, app.number("SELECT COUNT(*) FROM study_log"));
        assertEquals(1, app.number("SELECT current_streak FROM user_settings WHERE user_id = 1"));
    }

    private static SyncRecord card(String word, int box, long updatedAt) {
        return record(SyncType.CARD_PROGRESS, word, updatedAt, "box", box, "next_review_date", "2026-11-14",
                "first_learned_date", "2026-11-10", "last_reviewed_at", 1L, "review_count", 1);
    }

    private static SyncRecord deck(String uuid, String name) {
        return record(SyncType.DECK, uuid, 100, "name", name, "language_code", "en", "is_default", 0, "sort_order", 0,
                "created_at", 1L);
    }

    private static SyncRecord word(String uuid, String deck, String term, long updatedAt) {
        return record(SyncType.WORD, uuid, updatedAt, "deck_uuid", deck, "term", term, "meaning_vi", "nghĩa",
                "sort_order", 1, "created_at", 1L);
    }

    private static SyncRecord record(SyncType type, String key, long updatedAt, Object... fields) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < fields.length; i += 2) {
            map.put((String) fields[i], fields[i + 1]);
        }
        return new SyncRecord(type, key, updatedAt, false, 50, map);
    }

    private static final class FakeRemote extends SyncRemote {
        private final List<SyncRecord> pushed = new ArrayList<>();
        private final List<Integer> batchSizes = new ArrayList<>();
        private final List<String> calls = new ArrayList<>();
        private final Deque<PullPage> pages = new ArrayDeque<>();
        private Function<SyncRecord, PushResult> reply = sent ->
                new PushResult(PushResult.Status.APPLIED, 1, null, null);
        private Runnable beforeReply = () -> {
        };
        private ErrorCode error;

        private FakeRemote() {
            super(null);
        }

        @Override
        public Result<List<PushResult>> push(String deviceId, List<SyncRecord> records) {
            calls.add("push");
            if (error != null) {
                return Result.error(error);
            }
            pushed.addAll(records);
            batchSizes.add(records.size());
            beforeReply.run();
            return Result.ok(records.stream().map(reply).collect(Collectors.toList()));
        }

        @Override
        public Result<PullPage> pull(long since) {
            calls.add("pull");
            if (error != null) {
                return Result.error(error);
            }
            return Result.ok(pages.isEmpty() ? new PullPage(List.of(), since, false) : pages.poll());
        }
    }
}
