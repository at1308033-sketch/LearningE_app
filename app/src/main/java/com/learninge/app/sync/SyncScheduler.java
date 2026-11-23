package com.learninge.app.sync;

import android.os.SystemClock;

import com.learninge.app.core.AppExecutors;
import com.learninge.app.core.SessionStore;
import com.learninge.app.data.remote.NetworkMonitor;
import com.learninge.app.data.repo.SyncRepository;

/**
 * Thời điểm đồng bộ tự động (BR-077). Không chạy nền khi app tắt (DEC-014).
 */
public class SyncScheduler {
    private static final long FOREGROUND_INTERVAL_MILLIS = 5 * 60 * 1_000;

    private final SyncManager manager;
    private final SyncRepository repository;
    private final SessionStore session;
    private final NetworkMonitor network;
    private final AppExecutors executors;
    private final Runnable contentUpdate;
    private long lastForegroundAt;

    public SyncScheduler(SyncManager manager, SyncRepository repository, SessionStore session, NetworkMonitor network,
                         AppExecutors executors, Runnable contentUpdate) {
        this.manager = manager;
        this.repository = repository;
        this.session = session;
        this.network = network;
        this.executors = executors;
        this.contentUpdate = contentUpdate;
        network.addOnAvailable(this::onNetworkAvailable);
    }

    /**
     * Ngay sau khi đăng nhập hoặc đăng ký.
     */
    public void onSignedIn() {
        requestIfOnline();
    }

    /**
     * Mở app: nhiều nhất một lần mỗi 5 phút.
     */
    public void onForeground() {
        long now = SystemClock.elapsedRealtime();
        if (lastForegroundAt == 0 || now - lastForegroundAt >= FOREGROUND_INTERVAL_MILLIS) {
            lastForegroundAt = now;
            requestIfOnline();
            if (network.isOnline()) {
                executors.io().execute(contentUpdate);
            }
        }
    }

    /**
     * Sau phiên học, lượt quiz hoặc lượt thi.
     */
    public void onActivityFinished() {
        requestIfOnline();
    }

    /**
     * Nút "Đồng bộ ngay".
     */
    public void onManualRequest() {
        requestIfOnline();
    }

    private void onNetworkAvailable() {
        executors.io().execute(() -> {
            if (session.isSignedIn() && repository.countPending(session.userId()) > 0) {
                manager.requestSync();
            }
        });
    }

    private void requestIfOnline() {
        if (network.isOnline()) {
            manager.requestSync();
        }
    }
}
