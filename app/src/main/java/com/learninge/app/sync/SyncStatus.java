package com.learninge.app.sync;

/**
 * Lượt đồng bộ đang chạy tới đâu, cho SCR-35.
 */
public record SyncStatus(boolean isRunning, int pushed, int total) {
    public static final SyncStatus IDLE = new SyncStatus(false, 0, 0);
}
