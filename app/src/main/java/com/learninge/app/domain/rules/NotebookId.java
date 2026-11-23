package com.learninge.app.domain.rules;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Mã tất định của bộ "Sổ tay của tôi" (BR-046): server và mọi máy tự tính ra cùng một giá trị (UUIDv5).
 */
public final class NotebookId {
    private static final UUID NS_LEARNINGE = UUID.fromString("a7039332-6c5e-4d97-8f86-2dc4e4e084cd");

    private NotebookId() {
    }

    public static String of(String serverUserId) {
        return uuid5(NS_LEARNINGE, "notebook:" + serverUserId).toString();
    }

    private static UUID uuid5(UUID namespace, String name) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.update(ByteBuffer.allocate(16)
                    .putLong(namespace.getMostSignificantBits())
                    .putLong(namespace.getLeastSignificantBits())
                    .array());
            ByteBuffer hash = ByteBuffer.wrap(sha1.digest(name.getBytes(StandardCharsets.UTF_8)));
            // RFC 4122: 4 bit phiên bản = 5, 2 bit biến thể = 10.
            long high = (hash.getLong() & 0xffffffffffff0fffL) | 0x5000L;
            long low = (hash.getLong() & 0x3fffffffffffffffL) | 0x8000000000000000L;
            return new UUID(high, low);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
