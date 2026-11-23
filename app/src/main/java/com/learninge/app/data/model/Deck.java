package com.learninge.app.data.model;

/**
 * Bộ từ. {@code ownerUserId} null = bộ có sẵn, chỉ đọc (BR-043).
 */
public record Deck(long id, String uuid, String code, Long ownerUserId, String name, String description,
                   String languageCode, String level, String icon, String examTag, boolean isDefault, long createdAt) {

    public boolean isBuiltin() {
        return ownerUserId == null;
    }
}
