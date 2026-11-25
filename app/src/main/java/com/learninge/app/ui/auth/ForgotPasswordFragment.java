package com.learninge.app.ui.auth;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.R;
import com.learninge.app.databinding.FragmentForgotPasswordBinding;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.FeedbackBannerView;

/**
 * SCR-25 Quên mật khẩu — 2 bước trong 1 fragment.
 * Server chưa hỗ trợ reset password, hiển thị thông báo "đang phát triển".
 */
public class ForgotPasswordFragment extends BaseFragment {

    public ForgotPasswordFragment() {
        super(R.layout.fragment_forgot_password);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentForgotPasswordBinding binding = FragmentForgotPasswordBinding.bind(view);
        padStatusBar(binding.headerForgot);

        binding.toolbarForgot.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.btnForgotNext.setOnClickListener(v -> {
            String email = binding.etForgotEmail.getText() != null
                    ? binding.etForgotEmail.getText().toString().trim() : "";
            if (email.isEmpty()) {
                binding.tilForgotEmail.setError(getString(R.string.register_error_email));
                return;
            }
            binding.tilForgotEmail.setError(null);
            // Server chưa hỗ trợ → hiện thông báo
            binding.bannerForgot.show(FeedbackBannerView.Kind.INFO,
                    getString(R.string.forgot_developing));
        });

        binding.btnForgotSubmit.setOnClickListener(v ->
                binding.bannerForgot.show(FeedbackBannerView.Kind.INFO,
                        getString(R.string.forgot_developing)));
    }
}
