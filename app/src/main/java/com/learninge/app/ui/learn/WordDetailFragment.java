package com.learninge.app.ui.learn;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.learninge.app.NavMainDirections;
import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.data.model.Word;
import com.learninge.app.data.model.WordDetail;
import com.learninge.app.databinding.FragmentWordDetailBinding;
import com.learninge.app.ui.common.Badges;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;
import com.learninge.app.ui.common.HeartButton;
import com.learninge.app.ui.common.Speaker;
import com.learninge.app.ui.common.TtsHelper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * SCR-08 Chi tiết từ (US-008, US-010, US-011).
 */
public class WordDetailFragment extends BaseFragment {

    public WordDetailFragment() {
        super(R.layout.fragment_word_detail);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentWordDetailBinding binding = FragmentWordDetailBinding.bind(view);
        padStatusBar(binding.toolbarWord);
        binding.toolbarWord.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        TtsHelper tts = new TtsHelper(requireContext(), getViewLifecycleOwner());
        WordDetailViewModel viewModel = new ViewModelProvider(this).get(WordDetailViewModel.class);
        long wordId = WordDetailFragmentArgs.fromBundle(requireArguments()).getWordId();
        binding.btnFavorite.setOnClickListener(v -> viewModel.toggleFavorite(wordId));
        binding.btnSaveToDeck.setOnClickListener(v -> viewModel.loadTargets());
        viewModel.targets().observe(getViewLifecycleOwner(), decks -> chooseTarget(viewModel, wordId, decks));
        viewModel.copied().observe(getViewLifecycleOwner(), result -> Snackbar.make(view, result.isOk()
                ? getString(R.string.word_saved) : result.error == ErrorCode.DUPLICATE
                ? getString(R.string.word_error_duplicate) : ErrorMessages.of(requireContext(), result.error),
                Snackbar.LENGTH_LONG).show());
        viewModel.deleted().observe(getViewLifecycleOwner(), result -> {
            if (result.isOk()) {
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
        viewModel.word().observe(getViewLifecycleOwner(), result -> render(binding, tts, result));
        viewModel.load(wordId);
    }

    private void render(FragmentWordDetailBinding binding, TtsHelper tts, Result<WordDetail> result) {
        binding.svWord.setVisibility(result.isOk() ? View.VISIBLE : View.GONE);
        binding.esWord.setVisibility(result.isOk() ? View.GONE : View.VISIBLE);
        if (!result.isOk()) {
            binding.esWord.setTitle(ErrorMessages.of(requireContext(), result.error));
            return;
        }
        WordDetail detail = result.value;
        renderMenu(binding, detail);
        Word word = detail.item().word();
        Badges.showLevel(binding.tvLevel, detail.deck().level());
        showOrHide(binding.tvPos, word.partOfSpeech());
        binding.tvTerm.setText(word.term());
        showOrHide(binding.tvReading, word.reading());
        binding.tvMeaning.setText(word.meaning());
        boolean hasExample = word.example() != null;
        binding.cardExample.setVisibility(hasExample ? View.VISIBLE : View.GONE);
        binding.tvExample.setText(word.example());
        showOrHide(binding.tvExampleTranslation, word.exampleTranslation());
        Speaker.bind(binding.btnSpeak, tts, word.term(), detail.language());
        if (hasExample) {
            Speaker.bind(binding.btnSpeakExample, tts, word.example(), detail.language());
        }
        HeartButton.show(binding.btnFavorite, detail.item().isFavorite());
        Badges.showStatus(binding.tvStatus, detail.item().status());
        binding.tvBox.setText(boxText(detail));
    }

    private void chooseTarget(WordDetailViewModel viewModel, long wordId, List<DeckSummary> decks) {
        if (decks == null) {
            return;
        }
        viewModel.onTargetsShown();
        String[] names = decks.stream().map(item -> item.deck().name()).toArray(String[]::new);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.word_save_to_deck)
                .setItems(names, (dialog, which) -> viewModel.copyTo(wordId, decks.get(which).deck().id()))
                .setNegativeButton(R.string.common_cancel, null)
                .show();
    }

    // Chỉ từ trong bộ của người dùng mới sửa, xóa được (BR-043).
    private void renderMenu(FragmentWordDetailBinding binding, WordDetail detail) {
        boolean isMine = !detail.deck().isBuiltin();
        long wordId = detail.item().word().id();
        binding.toolbarWord.getMenu().findItem(R.id.action_edit_word).setVisible(isMine);
        binding.toolbarWord.getMenu().findItem(R.id.action_delete_word).setVisible(isMine);
        binding.toolbarWord.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_word) {
                NavHostFragment.findNavController(this).navigate(
                        NavMainDirections.actionGlobalWordEdit(detail.deck().id()).setWordId(wordId));
            } else {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.word_delete_title)
                        .setNegativeButton(R.string.common_cancel, null)
                        .setPositiveButton(R.string.common_delete, (dialog, which) ->
                                new ViewModelProvider(this).get(WordDetailViewModel.class).delete(wordId))
                        .show();
            }
            return true;
        });
    }

    private String boxText(WordDetail detail) {
        if (detail.item().box() == null) {
            return getString(R.string.word_box_new);
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(services().clock()),
                LocalDate.parse(detail.item().nextReviewDate()));
        return days <= 0 ? getString(R.string.word_box_due, detail.item().box())
                : getString(R.string.word_box_in_days, detail.item().box(), days);
    }

    private static void showOrHide(android.widget.TextView view, String text) {
        view.setText(text);
        view.setVisibility(text == null ? View.GONE : View.VISIBLE);
    }
}
