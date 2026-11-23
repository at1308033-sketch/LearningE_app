package com.learninge.app.data.model;

import java.util.Map;

/**
 * Một bản ghi đồng bộ ở dạng của app (không phải message proto). Tên trong {@code fields} là tên cột SQLite.
 *
 * @param key       uuid của bản ghi, hoặc uuid của từ (tiến độ, yêu thích), hoặc rỗng (cài đặt)
 * @param serverSeq 0 nếu chưa từng lên server
 */
public record SyncRecord(SyncType type, String key, long updatedAt, boolean isDeleted, long serverSeq,
                         Map<String, Object> fields) {

    public String text(String field) {
        return (String) fields.get(field);
    }

    public long number(String field) {
        Object value = fields.get(field);
        return value == null ? 0 : ((Number) value).longValue();
    }

    public boolean has(String field) {
        return fields.get(field) != null;
    }
}
