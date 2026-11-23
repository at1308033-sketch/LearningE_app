package com.learninge.app.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.databinding.FragmentRegisterBinding;
import com.learninge.app.domain.rules.AccountRules.Field;
import com.learninge.app.domain.rules.RegisterForm;

import java.util.Arrays;
import java.util.Set;

/**
 * SCR-03 Đăng ký (US-001).
 */
public class RegisterFragment extends AuthFormFragment {

    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentRegisterBinding binding = FragmentRegisterBinding.bind(view);
        padStatusBar(binding.headerRegister);
        String[] questions = getResources().getStringArray(R.array.security_questions);
        binding.actQuestion.setAdapter(new ArrayAdapter<>(requireContext(),
                com.google.android.material.R.layout.m3_auto_complete_simple_item, questions));
        AuthFormViewModel viewModel = new ViewModelProvider(this).get(AuthFormViewModel.class);
        viewModel.state().observe(getViewLifecycleOwner(), state -> render(binding, state));
        binding.toolbarRegister.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.btnRegister.setOnClickListener(v -> viewModel.register(form(binding, questions)));
    }

    private static RegisterForm form(FragmentRegisterBinding binding, String[] questions) {
        // indexOf trả -1 khi chưa chọn, +1 thành 0 = chưa chọn (BR-009: câu hỏi 1..5).
        int questionId = Arrays.asList(questions).indexOf(text(binding.tilQuestion)) + 1;
        return new RegisterForm(text(binding.tilEmail), text(binding.tilNickname), text(binding.tilDisplayName),
                text(binding.tilPassword), text(binding.tilConfirm), questionId, text(binding.tilAnswer));
    }

    private void render(FragmentRegisterBinding binding, AuthFormState state) {
        renderForm(state, binding.btnRegister, R.string.register_submit, binding.bannerRegister);
        Set<Field> errors = state.fieldErrors();
        showFieldError(binding.tilEmail, errors.contains(Field.EMAIL), state.error() == ErrorCode.EMAIL_TAKEN
                ? R.string.register_error_email_taken : R.string.register_error_email);
        showFieldError(binding.tilNickname, errors.contains(Field.NICKNAME), state.error() == ErrorCode.NICKNAME_TAKEN
                ? R.string.register_error_nickname_taken : R.string.register_error_nickname);
        showFieldError(binding.tilDisplayName, errors.contains(Field.DISPLAY_NAME),
                R.string.register_error_display_name);
        showFieldError(binding.tilPassword, errors.contains(Field.PASSWORD), R.string.register_error_password);
        showFieldError(binding.tilConfirm, errors.contains(Field.CONFIRM), R.string.register_error_confirm);
        showFieldError(binding.tilQuestion, errors.contains(Field.QUESTION), R.string.register_error_question);
        showFieldError(binding.tilAnswer, errors.contains(Field.ANSWER), R.string.register_error_answer);
    }
}
