package com.learninge.app.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.databinding.FragmentLoginBinding;
import com.learninge.app.domain.rules.AccountRules.Field;

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

    private void render(FragmentLoginBinding binding, AuthFormState state) {
        renderForm(state, binding.btnLogin, R.string.login_submit, binding.bannerLogin);
        showFieldError(binding.tilEmail, state.fieldErrors().contains(Field.EMAIL), R.string.register_error_email);
        showFieldError(binding.tilPassword, state.fieldErrors().contains(Field.PASSWORD),
                R.string.login_error_password_empty);
        binding.tilEmail.setEnabled(!state.isLoading());
        binding.tilPassword.setEnabled(!state.isLoading());
        if (state.error() == ErrorCode.INVALID_CREDENTIALS) {
            // Không nói rõ sai email hay mật khẩu (AC-002.2).
            binding.tilPassword.requestFocus();
        }
    }
}
