package com.learninge.app.ui.auth;

import android.app.Application;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.User;
import com.learninge.app.domain.rules.RegisterForm;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.function.Supplier;

/**
 * Gửi form đăng nhập/đăng ký trên luồng {@code io}; chặn bấm đúp khi đang gửi.
 */
public class AuthFormViewModel extends BaseViewModel {
    private final MutableLiveData<AuthFormState> state = new MutableLiveData<>(AuthFormState.IDLE);

    public AuthFormViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<AuthFormState> state() {
        return state;
    }

    void login(String email, String password) {
        submit(() -> services.authService().login(email, password));
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
            state.postValue(result.isOk() ? AuthFormState.DONE
                    : AuthFormState.failed(result, SystemClock.elapsedRealtime()));
        });
    }
}
