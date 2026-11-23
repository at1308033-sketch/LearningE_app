package com.learninge.app.sync;

import com.learninge.app.data.model.SyncState;

/**
 * Năm trạng thái của màn Đồng bộ (SCR-35).
 */
public enum SyncView {
    OFFLINE, SYNCING, ERROR, PENDING, SYNCED;

    /**
     * Ưu tiên: đang chạy, mất mạng, lần thử gần nhất lỗi, còn thay đổi chờ đẩy, đã khớp máy chủ.
     */
    public static SyncView of(boolean isOnline, SyncStatus status, SyncState state, int pending) {
        if (status.isRunning()) {
            return SYNCING;
        }
        if (!isOnline) {
            return OFFLINE;
        }
        boolean hasError = state != null && state.lastErrorAt() != null
                && (state.lastSyncAt() == null || state.lastErrorAt() > state.lastSyncAt());
        if (hasError) {
            return ERROR;
        }
        boolean isUpToDate = pending == 0 && state != null && state.lastSyncAt() != null;
        return isUpToDate ? SYNCED : PENDING;
    }
}
