package com.learninge.app.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.learninge.app.core.Result;
import com.learninge.app.core.ServiceLocator;

/**
 * Việc cần làm khi mở app, chạy nền để không chặn giao diện (NFR-P2).
 */
public class SplashViewModel extends ViewModel {

    enum State {
        LOADING, SIGNED_IN, SIGNED_OUT, SEED_FAILED
    }

    private final MutableLiveData<State> state = new MutableLiveData<>();

    LiveData<State> state() {
        return state;
    }

    /**
     * Nạp seed (FLOW-010) rồi kiểm tra phiên; gọi lại được khi nạp lỗi.
     */
    void start(ServiceLocator services) {
        if (state.getValue() != null && state.getValue() != State.SEED_FAILED) {
            return;
        }
        state.setValue(State.LOADING);
        services.executors().dbWrite().execute(() -> {
            Result<Void> seed = services.seedLoader().loadIfNeeded(services.installStamp());
            if (!seed.isOk()) {
                state.postValue(State.SEED_FAILED);
            } else {
                state.postValue(services.sessionStore().isSignedIn() ? State.SIGNED_IN : State.SIGNED_OUT);
            }
        });
    }
}
