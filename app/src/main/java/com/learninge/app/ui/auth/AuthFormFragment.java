package com.learninge.app.ui.auth;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;
import com.learninge.app.ui.common.FeedbackBannerView;
import com.learninge.app.ui.main.MainActivity;

/**
 * Phần chung của SCR-02 và SCR-03: nút gửi, thẻ lỗi, đếm ngược khóa tạm, vào app chính.
 */
abstract class AuthFormFragment extends BaseFragment {
    private CountDownTimer lockTimer;

    AuthFormFragment(@LayoutRes int layout) {
        super(layout);
    }

    @Override
    public void onDestroyView() {
        stopLockTimer();
        super.onDestroyView();
    }

    static String text(TextInputLayout field) {
        TextView input = field.getEditText();
        return input == null ? "" : input.getText().toString();
    }

    /**
     * Gọi lại với mọi trường mỗi lần trạng thái đổi: hết lỗi thì xóa chữ lỗi.
     */
    void showFieldError(TextInputLayout field, boolean hasError, @StringRes int message) {
        field.setError(hasError ? getString(message) : null);
    }

    void renderForm(AuthFormState state, MaterialButton submit, @StringRes int label, FeedbackBannerView banner) {
        stopLockTimer();
        submit.setEnabled(!state.isLoading());
        submit.setText(state.isLoading() ? R.string.common_sending : label);
        renderBanner(state, banner);
        if (state.isDone()) {
            startActivity(new Intent(requireContext(), MainActivity.class));
            requireActivity().finish();
        } else if (state.lockedUntil() > SystemClock.elapsedRealtime()) {
            startLockTimer(state.lockedUntil(), submit, label);
        }
    }

    void renderBanner(AuthFormState state, FeedbackBannerView banner) {
        boolean isShownAtField = state.error() == ErrorCode.EMAIL_TAKEN || state.error() == ErrorCode.NICKNAME_TAKEN;
        if (state.error() == null || isShownAtField) {
            banner.hide();
        } else {
            banner.show(FeedbackBannerView.Kind.ERROR, ErrorMessages.of(requireContext(), state.error()));
        }
    }

    private void startLockTimer(long until, MaterialButton submit, @StringRes int label) {
        submit.setEnabled(false);
        lockTimer = new CountDownTimer(until - SystemClock.elapsedRealtime(), 1_000) {
            @Override
            public void onTick(long millisLeft) {
                submit.setText(getString(R.string.login_locked, (millisLeft + 999) / 1_000));
            }

            @Override
            public void onFinish() {
                submit.setEnabled(true);
                submit.setText(label);
            }
        }.start();
    }

    private void stopLockTimer() {
        if (lockTimer != null) {
            lockTimer.cancel();
            lockTimer = null;
        }
    }
}
