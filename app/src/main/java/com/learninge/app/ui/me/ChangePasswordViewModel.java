package com.learninge.app.ui.me;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.ui.common.BaseViewModel;

/**
 * SCR-23 Đổi mật khẩu.
 */
public class ChangePasswordViewModel extends BaseViewModel {

    private final MutableLiveData<Result<Void>> result = new MutableLiveData<>();

    public ChangePasswordViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Result<Void>> result() {
        return result;
    }

    void change(String current, String newPwd, String confirm) {
        if (current.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
            result.setValue(Result.error(ErrorCode.VALIDATION, "EMPTY"));
            return;
        }
        if (!newPwd.equals(confirm)) {
            result.setValue(Result.error(ErrorCode.VALIDATION, "MISMATCH"));
            return;
        }
        services.executors().io().execute(() -> {
            Result<Void> r = services.authService().changePassword(current, newPwd);
            result.postValue(r);
        });
    }
}
