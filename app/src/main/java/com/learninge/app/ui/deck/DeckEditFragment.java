package com.learninge.app.ui.deck;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.Deck;
import com.learninge.app.data.model.Language;
import com.learninge.app.databinding.FragmentDeckEditBinding;
import com.learninge.app.domain.StudyService;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SCR-27 Tạo/sửa bộ (US-028, US-029).
 */
public class DeckEditFragment extends BaseFragment {
    private List<Language> languages = List.of();

    public DeckEditFragment() {
        super(R.layout.fragment_deck_edit);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentDeckEditBinding binding = FragmentDeckEditBinding.bind(view);
        padStatusBar(binding.headerDeckEdit);
        long deckId = DeckEditFragmentArgs.fromBundle(requireArguments()).getDeckId();
        binding.toolbarDeckEdit.setTitle(deckId == StudyService.NO_DECK ? R.string.deck_create_title
                : R.string.deck_edit_title);
        binding.toolbarDeckEdit.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        DeckEditViewModel viewModel = new ViewModelProvider(this).get(DeckEditViewModel.class);
        viewModel.form().observe(getViewLifecycleOwner(), form -> fill(binding, form, savedInstanceState == null));
        viewModel.saved().observe(getViewLifecycleOwner(), result -> onSaved(binding, result));
        binding.btnSaveDeck.setOnClickListener(v -> viewModel.save(deckId, textOf(binding.etDeckName),
                textOf(binding.etDeckDescription), languageCode(binding)));
        viewModel.load(deckId);
    }

    private void fill(FragmentDeckEditBinding binding, DeckEditViewModel.Form form, boolean isFirstTime) {
        languages = form.languages();
        List<String> names = languages.stream().map(Language::name).collect(Collectors.toList());
        binding.actLanguage.setAdapter(new ArrayAdapter<>(requireContext(),
                com.google.android.material.R.layout.m3_auto_complete_simple_item, names));
        boolean isLocked = form.deck() != null && form.deck().total() > 0;
        binding.tilLanguage.setEnabled(!isLocked);
        binding.tilLanguage.setHelperText(isLocked ? getString(R.string.deck_language_locked) : null);
        if (!isFirstTime) {
            return;
        }
        String code = form.deck() == null ? "en" : form.deck().deck().languageCode();
        languages.stream().filter(language -> language.code().equals(code)).findFirst()
                .ifPresent(language -> binding.actLanguage.setText(language.name(), false));
        if (form.deck() != null) {
            Deck deck = form.deck().deck();
            binding.etDeckName.setText(deck.name());
            binding.etDeckDescription.setText(deck.description());
        }
    }

    private String languageCode(FragmentDeckEditBinding binding) {
        String name = textOf(binding.actLanguage);
        return languages.stream().filter(language -> language.name().equals(name)).map(Language::code)
                .findFirst().orElse("en");
    }

    private void onSaved(FragmentDeckEditBinding binding, Result<Void> result) {
        boolean isNameWrong = result.error == ErrorCode.VALIDATION && result.detail.contains("NAME");
        boolean isLong = result.error == ErrorCode.VALIDATION && result.detail.contains("DESCRIPTION");
        binding.tilDeckName.setError(isNameWrong ? getString(R.string.deck_error_name)
                : result.error == ErrorCode.DUPLICATE ? getString(R.string.deck_error_duplicate) : null);
        binding.tilDeckDescription.setError(isLong ? getString(R.string.deck_error_description) : null);
        if (result.isOk()) {
            NavHostFragment.findNavController(this).navigateUp();
        } else if (result.error != ErrorCode.VALIDATION && result.error != ErrorCode.DUPLICATE) {
            Snackbar.make(binding.getRoot(), ErrorMessages.of(requireContext(), result.error), Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private static String textOf(android.widget.TextView view) {
        return view.getText().toString();
    }
}
