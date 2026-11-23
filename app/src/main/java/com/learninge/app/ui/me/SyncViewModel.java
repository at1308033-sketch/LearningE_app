package com.learninge.app.ui.me;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.BuildConfig;
import com.learninge.app.data.model.SyncState;
import com.learninge.app.sync.SyncStatus;
import com.learninge.app.sync.SyncView;
import com.learninge.app.ui.common.BaseViewModel;

/**
 * Trạng thái đồng bộ (US-041).
 */
public class SyncViewModel extends BaseViewModel {

    /**
     * @param server địa chỉ máy chủ, chỉ có ở bản debug, null ở bản release
     */
    record State(SyncView view, SyncStatus status, SyncState sync, int pending, String server) {
    }

    private final MutableLiveData<State> state = new MutableLiveData<>();

    public SyncViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<State> state() {
        return state;
    }

    LiveData<SyncStatus> syncStatus() {
        return services.syncManager().status();
    }

    LiveData<Boolean> online() {
        return services.networkMonitor().online();
    }

    /**
     * Đọc lại mỗi khi mạng hoặc lượt đồng bộ đổi trạng thái.
     */
    void refresh() {
        SyncStatus status = services.syncManager().status().getValue();
        services.executors().io().execute(() -> {
            long userId = services.sessionStore().userId();
            SyncState sync = services.syncRepository().state(userId);
            int pending = services.syncRepository().countPending(userId);
            SyncStatus current = status == null ? SyncStatus.IDLE : status;
            SyncView view = SyncView.of(services.networkMonitor().isOnline(), current, sync, pending);
            String server = BuildConfig.DEBUG ? services.grpcClient().config().display() : null;
            state.postValue(new State(view, current, sync, pending, server));
        });
    }

    void syncNow() {
        services.syncScheduler().onManualRequest();
    }
}
