package com.learninge.app.data.remote;

import com.learninge.app.data.model.SyncRecord;

/**
 * Kết quả server trả cho một bản ghi đã đẩy (BR-064).
 *
 * @param current bản của server, có với STALE và với sự kiện XP
 */
public record PushResult(Status status, long serverSeq, SyncRecord current, String errorCode) {

    public enum Status {
        APPLIED, STALE, DUPLICATE, REJECTED
    }
}
