package com.learninge.app.ui.auth;

import android.app.Application;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.User;
import com.learninge.app.domain.rules.RegisterForm;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.function.Supplier;

/**
 * Gửi form đăng nhập/đăng ký trên luồng {@code io}; chặn bấm đúp khi đang gửi.
 */
public class AuthFormViewModel extends BaseViewModel {
    private final MutableLiveData<String> lastEmail = new MutableLiveData<>();
    private final MutableLiveData<Integer> switchWarning = new MutableLiveData<>();
    private final MutableLiveData<AuthFormState> state = new MutableLiveData<>(AuthFormState.IDLE);

    public AuthFormViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<AuthFormState> state() {
        return state;
    }

    LiveData<String> lastEmail() {
        return lastEmail;
    }

    /**
     * Số thay đổi sẽ mất nếu đăng nhập tài khoản khác; null khi không cần hỏi.
     */
    LiveData<Integer> switchWarning() {
        return switchWarning;
    }

    void loadLastEmail() {
        services.executors().dbRead().execute(() -> lastEmail.postValue(services.authService().lastEmail()));
    }

    /**
     * Đăng nhập, hỏi trước nếu máy còn thay đổi chưa đồng bộ của tài khoản khác (BR-059).
     */
    void login(String email, String password) {
        services.executors().dbRead().execute(() -> {
            int lost = services.authService().pendingLostBySwitchingTo(email);
            services.executors().main().execute(() -> {
                if (lost > 0) {
                    switchWarning.setValue(lost);
                } else {
                    loginAnyway(email, password);
                }
            });
        });
    }

    void loginAnyway(String email, String password) {
        switchWarning.setValue(null);
        submit(() -> services.authService().login(email, password));
    }

    void cancelSwitch() {
        switchWarning.setValue(null);
    }

    /**
     * Có mạng trở lại thì bỏ cảnh báo mất mạng, giữ nguyên dữ liệu đã nhập.
     */
    void onNetworkChanged(boolean isOnline) {
        AuthFormState current = state.getValue();
        if (isOnline && current != null && current.error() == ErrorCode.NETWORK_OFFLINE) {
            state.setValue(AuthFormState.IDLE);
        }
    }

    void register(RegisterForm form) {
        submit(() -> services.authService().register(form));
    }

    private void submit(Supplier<Result<User>> request) {
        if (state.getValue() != null && state.getValue().isLoading()) {
            return;
        }
        state.setValue(AuthFormState.LOADING);
        services.executors().io().execute(() -> {
            Result<User> result = request.get();
            if (result.isOk()) {
                // Máy thứ hai kéo dữ liệu về ngay (AC-002.5).
                services.syncScheduler().onSignedIn();
            }
            state.postValue(result.isOk() ? AuthFormState.DONE
                    : AuthFormState.failed(result, SystemClock.elapsedRealtime()));
        });
    }
}
