package com.learninge.app.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.User;
import com.learninge.app.ui.common.BaseViewModel;

/**
 * SCR-25 Quên mật khẩu — EP-08 lấy câu hỏi, EP-09 đặt lại + đăng nhập.
 */
public class ForgotPasswordViewModel extends BaseViewModel {

    // questionId > 0 nghĩa là step 1 đã xong
    private final MutableLiveData<Result<Integer>> questionResult = new MutableLiveData<>();
    private final MutableLiveData<Result<User>> resetResult = new MutableLiveData<>();

    private String pendingEmail;

    public ForgotPasswordViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Result<Integer>> questionResult() { return questionResult; }
    LiveData<Result<User>> resetResult() { return resetResult; }

    /** Bước 1: gửi email → lấy câu hỏi bảo mật. */
    void getQuestion(String email) {
        pendingEmail = email.trim();
        services.executors().io().execute(() ->
                questionResult.postValue(services.authService().getResetQuestion(pendingEmail)));
    }

    /** Bước 2: gửi đáp án + mật khẩu mới → đăng nhập luôn. */
    void resetPassword(String answer, String newPassword) {
        services.executors().io().execute(() ->
                resetResult.postValue(
                        services.authService().resetPassword(pendingEmail, answer, newPassword)));
    }
}
