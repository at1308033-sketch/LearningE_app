package com.learninge.app.data.model;

/**
 * Trạng thái đồng bộ của người dùng trên máy này (SCR-35). Các mốc thời gian null = chưa có.
 */
public record SyncState(String deviceId, long cursor, Long lastSyncAt, String lastErrorCode, Long lastErrorAt) {
}
