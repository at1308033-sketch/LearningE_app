package com.learninge.app.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.learninge.app.core.ServiceLocator;

/**
 * Việc cần làm khi mở app, chạy nền để không chặn giao diện (NFR-P2).
 */
public class SplashViewModel extends ViewModel {
    private final MutableLiveData<Boolean> signedIn = new MutableLiveData<>();
    private boolean started;

    LiveData<Boolean> signedIn() {
        return signedIn;
    }

    void start(ServiceLocator services) {
        if (started) {
            return;
        }
        started = true;
        services.executors().io().execute(() -> signedIn.postValue(services.sessionStore().isSignedIn()));
    }
}
