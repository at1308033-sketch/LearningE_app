package com.learninge.app.data.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import com.learninge.app.data.model.SyncRecord;
import com.learninge.app.data.model.SyncType;
import com.learninge.proto.app.v1.Change;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SyncMapperTest {

    @Test
    public void everyType_survivesRoundTrip() {
        check(SyncType.SETTINGS, "level", "A2", "daily_goal", 15);
        check(SyncType.DECK, "name", "Bộ", "description", "mô tả", "language_code", "en", "icon", null, "is_default", 1,
                "sort_order", 3, "created_at", 5L);
        check(SyncType.WORD, "deck_uuid", "d", "term", "cat", "reading", null, "part_of_speech", "danh từ",
                "meaning_vi", "mèo", "example", "x", "example_translation", null, "sort_order", 1, "created_at", 2L);
        check(SyncType.CARD_PROGRESS, "box", 3, "next_review_date", "2026-11-14", "first_learned_date", "2026-11-10",
                "last_reviewed_at", 9L, "review_count", 4);
        check(SyncType.FAVORITE, "created_at", 7L);
        check(SyncType.STUDY_LOG, "activity_type", "REVIEW", "ref_uuid", null, "study_date", "2026-11-10",
                "created_at", 1L);
        check(SyncType.QUIZ_ATTEMPT, "source_type", "DECK", "deck_ref", "T01", "score", 80, "correct_count", 8,
                "total_count", 10, "duration_sec", 60, "created_at", 3L);
        check(SyncType.EXAM_ATTEMPT, "test_code", "E01", "score", 70, "correct_count", 14, "total_count", 20,
                "duration_sec", 800, "started_at", 1L, "submitted_at", 2L);
    }

    @Test
    public void xpEvent_neverSendsAwardedXp() {
        SyncRecord record = record(SyncType.XP_EVENT, "source_type", "QUIZ", "correct_count", 8, "wrong_count", 2,
                "total_count", 10, "occurred_at", 5L, "xp_awarded", 999);
        assertFalse(SyncMapper.toProto(record).getXpEvent().hasXpAwarded());
    }

    @Test
    public void unknownData_isIgnored() {
        assertNull(SyncMapper.fromProto(Change.getDefaultInstance()));
    }

    private static void check(SyncType type, Object... fields) {
        SyncRecord sent = record(type, fields);
        SyncRecord received = SyncMapper.fromProto(SyncMapper.toProto(sent));
        assertEquals(type, received.type());
        assertEquals("key", received.key());
        assertEquals(42, received.updatedAt());
        sent.fields().forEach((name, value) -> assertEquals(type + "." + name, String.valueOf(value),
                String.valueOf(received.fields().get(name))));
    }

    private static SyncRecord record(SyncType type, Object... fields) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < fields.length; i += 2) {
            map.put((String) fields[i], fields[i + 1]);
        }
        return new SyncRecord(type, "key", 42, false, 0, map);
    }
}
