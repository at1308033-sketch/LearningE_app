package com.learninge.app.ui.deck;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.databinding.FragmentWordEditBinding;
import com.learninge.app.domain.rules.DeckRules.Field;
import com.learninge.app.domain.rules.WordInput;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * SCR-20 Thêm/sửa từ trong bộ của người dùng (US-030).
 */
public class WordEditFragment extends BaseFragment {

    public WordEditFragment() {
        super(R.layout.fragment_word_edit);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentWordEditBinding binding = FragmentWordEditBinding.bind(view);
        padStatusBar(binding.headerWordEdit);
        WordEditFragmentArgs args = WordEditFragmentArgs.fromBundle(requireArguments());
        binding.toolbarWordEdit.setTitle(args.getWordId() == WordEditViewModel.NEW_WORD ? R.string.word_add_title
                : R.string.word_edit_title);
        binding.toolbarWordEdit.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        WordEditViewModel viewModel = new ViewModelProvider(this).get(WordEditViewModel.class);
        viewModel.form().observe(getViewLifecycleOwner(), form -> fill(binding, form, savedInstanceState == null));
        viewModel.saved().observe(getViewLifecycleOwner(), result -> onSaved(binding, result));
        binding.btnSaveWord.setOnClickListener(v -> viewModel.save(args.getDeckId(), args.getWordId(),
                new WordInput(textOf(binding.tilTerm), textOf(binding.tilReading), textOf(binding.tilMeaning),
                        textOf(binding.tilExample), textOf(binding.tilTranslation))));
        viewModel.load(args.getDeckId(), args.getWordId());
    }

    private void fill(FragmentWordEditBinding binding, WordEditViewModel.Form form, boolean isFirstTime) {
        binding.toolbarWordEdit.setSubtitle(getString(R.string.word_edit_subtitle, form.deck().deck().name(),
                form.deck().deck().languageCode().toUpperCase(Locale.ROOT)));
        // Nhãn cách đọc theo ngôn ngữ của bộ (BR-052).
        binding.tilReading.setHelperText(form.language().readingLabel());
        if (isFirstTime && form.word() != null) {
            binding.etTerm.setText(form.word().term());
            binding.etReading.setText(form.word().reading());
            binding.etMeaning.setText(form.word().meaning());
            binding.etExample.setText(form.word().example());
            binding.etTranslation.setText(form.word().exampleTranslation());
        }
    }

    private void onSaved(FragmentWordEditBinding binding, Result<Void> result) {
        if (result.isOk()) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }
        List<String> wrong = result.error == ErrorCode.VALIDATION && result.detail != null
                ? Arrays.asList(result.detail.split(",")) : List.of();
        showError(binding.tilTerm, wrong, Field.TERM, R.string.word_error_term);
        showError(binding.tilReading, wrong, Field.READING, R.string.word_error_reading);
        showError(binding.tilMeaning, wrong, Field.MEANING, R.string.word_error_meaning);
        showError(binding.tilExample, wrong, Field.EXAMPLE, R.string.word_error_example);
        showError(binding.tilTranslation, wrong, Field.EXAMPLE_TRANSLATION, R.string.word_error_example);
        if (result.error == ErrorCode.DUPLICATE) {
            binding.tilTerm.setError(getString(R.string.word_error_duplicate));
        } else if (result.error != ErrorCode.VALIDATION) {
            Snackbar.make(binding.getRoot(), ErrorMessages.of(requireContext(), result.error), Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void showError(TextInputLayout field, List<String> wrong, Field name, @StringRes int message) {
        field.setError(wrong.contains(name.name()) ? getString(message) : null);
    }

    private static String textOf(TextInputLayout field) {
        return field.getEditText() == null ? "" : field.getEditText().getText().toString();
    }
}
