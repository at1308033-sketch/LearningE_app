package com.learninge.app.core;

import android.content.Context;

import java.time.Clock;

/**
 * Nơi duy nhất tạo đối tượng dùng chung (DEC-001). Mọi thứ được tạo một lần khi ứng dụng khởi động.
 */
public final class ServiceLocator {
    private final AppExecutors executors = new AppExecutors();
    private final Clock clock = Clock.systemDefaultZone();
    private final SessionStore sessionStore;

    public ServiceLocator(Context context) {
        sessionStore = new SessionStore(context);
    }

    public AppExecutors executors() {
        return executors;
    }

    /**
     * Giờ của máy; đổi múi giờ khi app đang chạy thì ngày học vẫn theo múi giờ mới (BR-042).
     */
    public Clock clock() {
        return clock.withZone(java.time.ZoneId.systemDefault());
    }

    public SessionStore sessionStore() {
        return sessionStore;
    }
}
