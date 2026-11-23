package com.learninge.app.data.remote;

import com.learninge.app.data.model.SyncRecord;

import java.util.List;

/**
 * Một trang thay đổi kéo về (EP-12).
 */
public record PullPage(List<SyncRecord> records, long nextCursor, boolean hasMore) {
}
