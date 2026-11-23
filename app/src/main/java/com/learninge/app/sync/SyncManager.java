package com.learninge.app.sync;

import android.database.SQLException;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.AppExecutors;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.core.SessionStore;
import com.learninge.app.data.model.SyncRecord;
import com.learninge.app.data.remote.PullPage;
import com.learninge.app.data.remote.PushResult;
import com.learninge.app.data.remote.SyncRemote;
import com.learninge.app.data.repo.SyncRepository;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Một lượt đồng bộ: đẩy hết lô rồi kéo hết trang (BR-062..BR-064). Chỉ một lượt chạy cùng lúc; yêu cầu mới trong lúc
 * chạy được gộp thành đúng một lượt tiếp theo (BR-077).
 */
public class SyncManager {
    private static final String TAG = "SyncManager";
    private static final int BATCH_SIZE = 500;

    private final Supplier<SyncRemote> remote;
    private final SyncRepository repository;
    private final SessionStore session;
    private final AppExecutors executors;
    private final Clock clock;
    private final MutableLiveData<SyncStatus> status = new MutableLiveData<>(SyncStatus.IDLE);
    private boolean isRunning;
    private boolean isRequestedAgain;

    public SyncManager(Supplier<SyncRemote> remote, SyncRepository repository, SessionStore session,
                       AppExecutors executors, Clock clock) {
        this.remote = remote;
        this.repository = repository;
        this.session = session;
        this.executors = executors;
        this.clock = clock;
    }

    public LiveData<SyncStatus> status() {
        return status;
    }

    /**
     * Đặt một lượt đồng bộ lên luồng {@code io}, trả về ngay.
     */
    public void requestSync() {
        synchronized (this) {
            if (isRunning) {
                isRequestedAgain = true;
                return;
            }
            isRunning = true;
        }
        executors.io().execute(this::runUntilQuiet);
    }

    private void runUntilQuiet() {
        boolean shouldRunAgain = true;
        while (shouldRunAgain) {
            boolean hasSucceeded = syncOnce().isOk();
            synchronized (this) {
                // Lỗi thì dừng tới lần kích hoạt sau, không lặp ngay.
                shouldRunAgain = isRequestedAgain && hasSucceeded;
                isRequestedAgain = false;
                isRunning = shouldRunAgain;
            }
        }
        status.postValue(SyncStatus.IDLE);
    }

    /**
     * Chạy một lượt ngay trên luồng hiện tại (luồng {@code io}).
     */
    public Result<Void> syncOnce() {
        if (!session.isSignedIn()) {
            return Result.error(ErrorCode.UNAUTHENTICATED);
        }
        long userId = session.userId();
        try {
            Result<Void> result = run(userId);
            if (session.userId() == userId) {
                executors.write(() -> {
                    repository.finish(userId, result.isOk() ? null : result.error.name(), clock.millis());
                    return null;
                });
            }
            return result;
        } catch (SQLException failed) {
            Log.w(TAG, "sync failed in database", failed);
            return Result.error(ErrorCode.DB);
        }
    }

    private Result<Void> run(long userId) {
        status.postValue(new SyncStatus(true, 0, repository.countPending(userId)));
        if (repository.state(userId).cursor() == 0) {
            // Lần đầu trên máy này: kéo trước để biết server đã có gì (bộ mặc định, dữ liệu của máy khác).
            Result<Void> first = pullAll(userId);
            if (!first.isOk()) {
                return first;
            }
            executors.write(() -> {
                repository.queueMissingNotebook(userId);
                return null;
            });
        }
        Result<Void> pushed = pushAll(userId);
        return pushed.isOk() ? pullAll(userId) : pushed;
    }

    private Result<Void> pushAll(long userId) {
        String deviceId = repository.state(userId).deviceId();
        int total = repository.countPending(userId);
        int done = 0;
        while (true) {
            List<SyncRecord> batch = repository.nextBatch(userId, BATCH_SIZE);
            if (batch.isEmpty() || done >= total) {
                // done >= total: người dùng sửa tiếp trong lúc đẩy, để lượt sau.
                return Result.ok(null);
            }
            status.postValue(new SyncStatus(true, done, total));
            Result<List<PushResult>> results = remote.get().push(deviceId, batch);
            if (!results.isOk()) {
                return Result.errorOf(results);
            }
            String rejected = executors.write(() -> repository.applyPush(userId, batch, results.value));
            if (rejected != null) {
                Log.w(TAG, "server rejected a record: " + rejected);
                executors.write(() -> {
                    repository.saveRejected(userId, rejected, clock.millis());
                    return null;
                });
            }
            done += batch.size();
        }
    }

    private Result<Void> pullAll(long userId) {
        List<SyncRecord> waiting = new ArrayList<>();
        boolean hasMore = true;
        while (hasMore) {
            Result<PullPage> page = remote.get().pull(repository.state(userId).cursor());
            if (!page.isOk()) {
                return Result.errorOf(page);
            }
            executors.write(() -> {
                repository.applyPull(userId, page.value, waiting);
                return null;
            });
            hasMore = page.value.hasMore();
        }
        if (!waiting.isEmpty()) {
            Log.w(TAG, waiting.size() + " records skipped: parent not on this device");
        }
        return Result.ok(null);
    }
}
