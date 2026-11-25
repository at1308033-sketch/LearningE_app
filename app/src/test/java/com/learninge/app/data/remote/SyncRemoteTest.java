package com.learninge.app.data.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.learninge.app.data.model.SyncRecord;
import com.learninge.app.data.model.SyncType;
import com.learninge.proto.app.v1.PushRequest;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SyncRemoteTest {
    private static final int SERVER_LIMIT = 1024 * 1024;

    @Test
    public void smallRecords_goTogether() {
        assertEquals(500, SyncRemote.firstFitting(words(500, 10)).size());
        assertTrue(SyncRemote.firstFitting(List.of()).isEmpty());
    }

    @Test
    public void largeRecords_stayUnderServerLimit() {
        // Từ dài nhất cho phép, mọi ký tự 3 byte: 500 từ vượt 1 MiB.
        List<SyncRecord> all = words(500, 900);
        assertTrue(size(all) > SERVER_LIMIT);
        List<SyncRecord> first = SyncRemote.firstFitting(all);
        assertTrue(size(first) < SERVER_LIMIT);
        assertTrue(size(first) > SERVER_LIMIT / 2);
        assertEquals(all.subList(0, first.size()), first);
    }

    @Test
    public void oneHugeRecord_isStillSent() {
        assertEquals(1, SyncRemote.firstFitting(words(2, 500_000)).size());
    }

    private static int size(List<SyncRecord> records) {
        return PushRequest.newBuilder().addAllChanges(records.stream().map(SyncMapper::toProto)
                .collect(Collectors.toList())).build().getSerializedSize();
    }

    private static List<SyncRecord> words(int count, int length) {
        Map<String, Object> fields = Map.of("deck_uuid", "d", "term", "cat", "meaning_vi",
                String.join("", Collections.nCopies(length, "ế")), "sort_order", 1, "created_at", 2L);
        return Collections.nCopies(count, new SyncRecord(SyncType.WORD, "w", 1, false, 0, fields));
    }
}
