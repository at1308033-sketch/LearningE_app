package com.learninge.app.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.databinding.FragmentLoginBinding;
import com.learninge.app.domain.rules.AccountRules.Field;
import com.learninge.app.ui.common.FeedbackBannerView;

/**
 * SCR-02 Đăng nhập (US-002).
 */
public class LoginFragment extends AuthFormFragment {

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    protected boolean hasDarkHeader() {
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentLoginBinding binding = FragmentLoginBinding.bind(view);
        padStatusBar(binding.headerLogin);
        AuthFormViewModel viewModel = new ViewModelProvider(this).get(AuthFormViewModel.class);
        viewModel.state().observe(getViewLifecycleOwner(), state -> render(binding, state));
        services().networkMonitor().online().observe(getViewLifecycleOwner(), viewModel::onNetworkChanged);
        viewModel.switchWarning().observe(getViewLifecycleOwner(), lost -> confirmSwitch(binding, viewModel, lost));
        viewModel.lastEmail().observe(getViewLifecycleOwner(), email -> {
            if (email != null && text(binding.tilEmail).isEmpty()) {
                binding.etEmail.setText(email);
            }
        });
        if (savedInstanceState == null) {
            viewModel.loadLastEmail();
        }
        binding.btnLogin.setOnClickListener(v -> submit(binding, viewModel));
        binding.etPassword.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_DONE && binding.btnLogin.isEnabled()) {
                submit(binding, viewModel);
            }
            return false;
        });
        binding.btnToRegister.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(LoginFragmentDirections.actionLoginToRegister()));
    }

    private void submit(FragmentLoginBinding binding, AuthFormViewModel viewModel) {
        viewModel.login(text(binding.tilEmail), text(binding.tilPassword));
    }

    private void confirmSwitch(FragmentLoginBinding binding, AuthFormViewModel viewModel, Integer lost) {
        if (lost == null) {
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.login_switch_title)
                .setMessage(getResources().getQuantityString(R.plurals.login_switch_message, lost, lost))
                .setNegativeButton(R.string.login_switch_stay, (dialog, which) -> viewModel.cancelSwitch())
                .setPositiveButton(R.string.login_switch_continue, (dialog, which) ->
                        viewModel.loginAnyway(text(binding.tilEmail), text(binding.tilPassword)))
                .setCancelable(false)
                .show();
    }

    private void render(FragmentLoginBinding binding, AuthFormState state) {
        renderForm(state, binding.btnLogin, R.string.login_submit, binding.bannerLogin);
        showEndReason(binding, state);
        showFieldError(binding.tilEmail, state.fieldErrors().contains(Field.EMAIL), R.string.register_error_email);
        showFieldError(binding.tilPassword, state.fieldErrors().contains(Field.PASSWORD),
                R.string.login_error_password_empty);
        setFieldsEnabled(!state.isLoading(), binding.tilEmail, binding.tilPassword);
        if (state.error() == ErrorCode.INVALID_CREDENTIALS) {
            // Không nói rõ sai email hay mật khẩu (AC-002.2).
            binding.tilPassword.requestFocus();
        }
    }

    // Chỉ hiện lý do khi người dùng chưa gửi form lần nào.
    private void showEndReason(FragmentLoginBinding binding, AuthFormState state) {
        ErrorCode reason = ((AuthActivity) requireActivity()).endReason();
        if (state != AuthFormState.IDLE || reason == null) {
            return;
        }
        binding.bannerLogin.show(FeedbackBannerView.Kind.INFO, getString(reason == ErrorCode.ACCOUNT_DELETED
                ? R.string.error_account_deleted : R.string.login_session_expired));
    }
}
