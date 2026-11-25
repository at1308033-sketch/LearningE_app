package com.learninge.app.sync;

import static org.junit.Assert.assertEquals;

import com.learninge.app.data.model.SyncState;

import org.junit.Test;

public class SyncViewTest {
    private static final SyncStatus RUNNING = new SyncStatus(true, 12, 20);

    @Test
    public void running_wins() {
        assertEquals(SyncView.SYNCING, SyncView.of(true, RUNNING, state(1L, 5L), 3));
    }

    @Test
    public void offline_beforeErrorAndPending() {
        assertEquals(SyncView.OFFLINE, SyncView.of(false, SyncStatus.IDLE, state(1L, 5L), 3));
    }

    @Test
    public void error_onlyWhenNewerThanLastSuccess() {
        assertEquals(SyncView.ERROR, SyncView.of(true, SyncStatus.IDLE, state(1L, 5L), 0));
        assertEquals(SyncView.ERROR, SyncView.of(true, SyncStatus.IDLE, state(null, 5L), 0));
        assertEquals(SyncView.SYNCED, SyncView.of(true, SyncStatus.IDLE, state(9L, 5L), 0));
    }

    @Test
    public void pending_whenChangesWaitOrNeverSynced() {
        assertEquals(SyncView.PENDING, SyncView.of(true, SyncStatus.IDLE, state(9L, null), 2));
        assertEquals(SyncView.PENDING, SyncView.of(true, SyncStatus.IDLE, state(null, null), 0));
        assertEquals(SyncView.PENDING, SyncView.of(true, SyncStatus.IDLE, null, 0));
    }

    private static SyncState state(Long lastSyncAt, Long lastErrorAt) {
        return new SyncState("device", 0, lastSyncAt, lastErrorAt == null ? null : "DB", lastErrorAt, 1);
    }
}
