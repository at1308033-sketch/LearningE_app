package com.learninge.app.ui.learn;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.R;
import com.learninge.app.core.Result;
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
