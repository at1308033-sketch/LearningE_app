package com.learninge.app.ui.me;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.databinding.FragmentChangePasswordBinding;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;
import com.learninge.app.ui.common.FeedbackBannerView;

import java.util.Objects;

/**
 * SCR-23 Đổi mật khẩu.
 */
public class ChangePasswordFragment extends BaseFragment {

    public ChangePasswordFragment() {
        super(R.layout.fragment_change_password);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentChangePasswordBinding binding = FragmentChangePasswordBinding.bind(view);
        padStatusBar(binding.headerChangePassword);
        binding.toolbarChangePassword.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        ChangePasswordViewModel vm = new ViewModelProvider(this).get(ChangePasswordViewModel.class);
        vm.result().observe(getViewLifecycleOwner(), result -> {
            binding.btnSavePassword.setEnabled(true);
            if (result == null) return;
            if (result.isOk()) {
                binding.bannerChangePassword.show(FeedbackBannerView.Kind.SUCCESS,
                        getString(R.string.change_password_success));
                binding.etCurrentPassword.setText("");
                binding.etNewPassword.setText("");
                binding.etConfirmPassword.setText("");
            } else if (result.error == ErrorCode.VALIDATION && "MISMATCH".equals(result.detail)) {
                binding.bannerChangePassword.show(FeedbackBannerView.Kind.ERROR,
                        getString(R.string.change_password_mismatch));
            } else if (result.error == ErrorCode.VALIDATION && "EMPTY".equals(result.detail)) {
                binding.bannerChangePassword.show(FeedbackBannerView.Kind.ERROR,
                        getString(R.string.change_password_empty));
            } else {
                binding.bannerChangePassword.show(FeedbackBannerView.Kind.ERROR,
                        ErrorMessages.of(requireContext(), result.error));
            }
        });

        binding.btnSavePassword.setOnClickListener(v -> {
            hideKeyboard(v);
            binding.bannerChangePassword.hide();
            binding.btnSavePassword.setEnabled(false);
            String current = text(binding.etCurrentPassword.getText());
            String newPwd = text(binding.etNewPassword.getText());
            String confirm = text(binding.etConfirmPassword.getText());
            vm.change(current, newPwd, confirm);
        });
    }

    private static String text(@Nullable CharSequence s) {
        return s == null ? "" : s.toString();
    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = requireContext().getSystemService(InputMethodManager.class);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
