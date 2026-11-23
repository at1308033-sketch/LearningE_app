package com.learninge.app.data.model;

/**
 * Hồ sơ cục bộ. {@code serverUuid} là {@code user_id} trên máy chủ.
 */
public record User(long id, String serverUuid, String email, String nickname, String displayName, boolean isOnboarded) {
}
