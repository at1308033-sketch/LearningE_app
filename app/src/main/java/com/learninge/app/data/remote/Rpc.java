package com.learninge.app.data.remote;

/**
 * Mọi RPC app gọi, kèm deadline và quyền thử lại (SYSTEM-BLUEPRINT §Deadline và thử lại).
 */
public enum Rpc {
    REGISTER(10, false, true),
    LOGIN(10, false, true),
    LOGOUT(10, false, false),
    GET_ME(10, true, false),
    UPDATE_ME(10, false, false),
    CHANGE_PASSWORD(10, false, false),
    GET_RESET_QUESTION(10, false, true),
    RESET_PASSWORD(10, false, true),
    DELETE_ACCOUNT(10, false, false),
    PUSH(20, true, false),
    PULL(15, true, false),
    GET_WEEKLY(5, true, false),
    GET_MANIFEST(15, true, false),
    GET_DECK(15, true, false),
    GET_EXAM(15, true, false);

    final int deadlineSeconds;
    /**
     * Chỉ RPC idempotent mới được thử lại, để không đếm sai lần đăng nhập hay tạo trùng.
     */
    final boolean isIdempotent;
    /**
     * RPC công khai không gửi token.
     */
    final boolean isPublic;

    Rpc(int deadlineSeconds, boolean isIdempotent, boolean isPublic) {
        this.deadlineSeconds = deadlineSeconds;
        this.isIdempotent = isIdempotent;
        this.isPublic = isPublic;
    }
}
