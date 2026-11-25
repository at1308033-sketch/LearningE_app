package com.learninge.app.data.remote;

import com.learninge.app.data.model.SyncRecord;

import java.util.List;

/**
 * Một trang thay đổi kéo về (EP-12).
 *
 * @param dataEpoch kỷ nguyên dữ liệu của máy chủ, 0 nếu máy chủ bản cũ không gửi
 */
public record PullPage(List<SyncRecord> records, long nextCursor, boolean hasMore, long dataEpoch) {
}
