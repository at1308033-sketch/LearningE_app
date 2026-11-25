package com.learninge.app.ui.auth;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.databinding.FragmentForgotPasswordBinding;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.FeedbackBannerView;

/**
 * SCR-25 Quên mật khẩu — 2 bước: (1) nhập email → lấy câu hỏi bảo mật, (2) trả lời + mật khẩu mới.
 */
public class ForgotPasswordFragment extends BaseFragment {

    public ForgotPasswordFragment() {
        super(R.layout.fragment_forgot_password);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentForgotPasswordBinding binding = FragmentForgotPasswordBinding.bind(view);
        padStatusBar(binding.headerForgot);

        ForgotPasswordViewModel vm = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        binding.toolbarForgot.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        // Bước 1 → lấy câu hỏi bảo mật
        binding.btnForgotNext.setOnClickListener(v -> {
            String email = text(binding.etForgotEmail);
            if (email.isEmpty()) {
                binding.tilForgotEmail.setError(getString(R.string.register_error_email));
                return;
            }
            binding.tilForgotEmail.setError(null);
            binding.btnForgotNext.setEnabled(false);
            vm.getQuestion(email);
        });

        vm.questionResult().observe(getViewLifecycleOwner(), result -> {
            binding.btnForgotNext.setEnabled(true);
            if (result.isOk()) {
                // Hiện câu hỏi bảo mật tương ứng questionId (1-based)
                String[] questions = getResources().getStringArray(R.array.security_questions);
                int idx = result.value - 1;
                String question = (idx >= 0 && idx < questions.length) ? questions[idx] : "";
                binding.tvForgotQuestion.setText(question);
                binding.stepEmail.setVisibility(View.GONE);
                binding.stepReset.setVisibility(View.VISIBLE);
            } else {
                String msg = result.error == ErrorCode.NOT_FOUND
                        ? getString(R.string.forgot_email_not_found)
                        : getString(R.string.error_network_timeout);
                binding.bannerForgot.show(FeedbackBannerView.Kind.ERROR, msg);
            }
        });

        // Bước 2 → đặt lại mật khẩu
        binding.btnForgotSubmit.setOnClickListener(v -> {
            String answer = text(binding.etForgotAnswer);
            String newPwd = text(binding.etForgotNewPassword);
            if (answer.isEmpty()) {
                binding.tilForgotAnswer.setError(getString(R.string.forgot_error_answer));
                return;
            }
            if (newPwd.isEmpty()) {
                binding.tilForgotNewPassword.setError(getString(R.string.register_error_password));
                return;
            }
            binding.tilForgotAnswer.setError(null);
            binding.tilForgotNewPassword.setError(null);
            binding.btnForgotSubmit.setEnabled(false);
            vm.resetPassword(answer, newPwd);
        });

        vm.resetResult().observe(getViewLifecycleOwner(), result -> {
            binding.btnForgotSubmit.setEnabled(true);
            if (result.isOk()) {
                // Đăng nhập xong → về Login (pop back stack)
                NavHostFragment.findNavController(this).navigateUp();
            } else {
                String msg = result.error == ErrorCode.VALIDATION
                        ? getString(R.string.forgot_error_answer)
                        : getString(R.string.error_network_timeout);
                binding.bannerForgot.show(FeedbackBannerView.Kind.ERROR, msg);
            }
        });
    }

    private static String text(android.widget.EditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
