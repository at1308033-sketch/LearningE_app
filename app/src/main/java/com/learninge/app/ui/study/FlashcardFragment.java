package com.learninge.app.ui.study;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.data.model.SessionSummary;
import com.learninge.app.data.model.Word;
import com.learninge.app.data.model.WordDetail;
import com.learninge.app.databinding.FragmentFlashcardBinding;
import com.learninge.app.ui.common.Badges;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;
import com.learninge.app.ui.common.Motion;
import com.learninge.app.ui.common.Speaker;
import com.learninge.app.ui.common.TtsHelper;

/**
 * SCR-09 Flashcard, dùng chung cho học từ mới và ôn tập (US-009, US-012).
 */
public class FlashcardFragment extends BaseFragment {
    // Hai nửa 175 ms = 350 ms (AC-009.2: không quá 400 ms).
    private static final int HALF_FLIP_MILLIS = 175;
    private static final float CAMERA_DISTANCE_DP = 8000;

    private FlashcardViewModel.State shown;

    public FlashcardFragment() {
        super(R.layout.fragment_flashcard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentFlashcardBinding binding = FragmentFlashcardBinding.bind(view);
        shown = null;
        padStatusBar(binding.headerFlashcard);
        binding.cardFlash.setCameraDistance(CAMERA_DISTANCE_DP * getResources().getDisplayMetrics().density);
        TtsHelper tts = new TtsHelper(requireContext(), getViewLifecycleOwner());
        FlashcardFragmentArgs args = FlashcardFragmentArgs.fromBundle(requireArguments());
        StudySource source = args.getSource();
        binding.tvSource.setText(source == StudySource.NEW ? R.string.study_new : R.string.study_review);
        FlashcardViewModel viewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);
        binding.btnClose.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.cardFlash.setOnClickListener(v -> viewModel.flip());
        binding.btnForgot.setOnClickListener(v -> viewModel.rate(false));
        binding.btnRemembered.setOnClickListener(v -> viewModel.rate(true));
        viewModel.state().observe(getViewLifecycleOwner(), state -> render(binding, tts, source, state));
        viewModel.finished().observe(getViewLifecycleOwner(), this::openResult);
        viewModel.start(source, args.getDeckId());
    }

    private void render(FragmentFlashcardBinding binding, TtsHelper tts, StudySource source,
                        FlashcardViewModel.State state) {
        boolean isEmpty = state.cards().isEmpty();
        binding.groupStudy.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.esFlashcard.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (isEmpty) {
            renderEmpty(binding, source, state.error());
            return;
        }
        if (state.error() != null) {
            Snackbar.make(binding.getRoot(), ErrorMessages.of(requireContext(), state.error()),
                    Snackbar.LENGTH_LONG).show();
        }
        binding.piSession.setMax(state.cards().size());
        binding.piSession.setProgressCompat(state.index(), true);
        binding.tvPosition.setText(getString(R.string.study_position, state.index() + 1, state.cards().size()));
        binding.llRating.setVisibility(state.isFlipped() ? View.VISIBLE : View.INVISIBLE);
        boolean isFlipOfSameCard = shown != null && shown.index() == state.index()
                && shown.isFlipped() != state.isFlipped();
        if (isFlipOfSameCard && Motion.isEnabled(requireContext())) {
            flip(binding, tts, state);
        } else {
            binding.cardFlash.setRotationY(0);
            showSide(binding, tts, state);
        }
        shown = state;
    }

    private void renderEmpty(FragmentFlashcardBinding binding, StudySource source, ErrorCode error) {
        if (error != ErrorCode.NOTHING_TO_STUDY) {
            binding.esFlashcard.setTitle(ErrorMessages.of(requireContext(), error));
        } else {
            binding.esFlashcard.setTitle(source == StudySource.NEW ? R.string.study_no_new : R.string.study_no_due);
        }
        binding.esFlashcard.setAction(R.string.common_back,
                v -> NavHostFragment.findNavController(this).navigateUp());
    }

    private void flip(FragmentFlashcardBinding binding, TtsHelper tts, FlashcardViewModel.State state) {
        ObjectAnimator out = ObjectAnimator.ofFloat(binding.cardFlash, View.ROTATION_Y, 0, 90);
        out.setDuration(HALF_FLIP_MILLIS);
        out.setInterpolator(new FastOutSlowInInterpolator());
        Motion.onEnd(out, () -> {
            if (getView() == null) {
                return;
            }
            showSide(binding, tts, state);
            ObjectAnimator in = ObjectAnimator.ofFloat(binding.cardFlash, View.ROTATION_Y, -90, 0);
            in.setDuration(HALF_FLIP_MILLIS);
            in.start();
        });
        out.start();
    }

    private void showSide(FragmentFlashcardBinding binding, TtsHelper tts, FlashcardViewModel.State state) {
        WordDetail card = state.card();
        Word word = card.item().word();
        binding.tvDeck.setText(card.deck().name());
        binding.llFront.setVisibility(state.isFlipped() ? View.GONE : View.VISIBLE);
        binding.llBack.setVisibility(state.isFlipped() ? View.VISIBLE : View.GONE);
        binding.tvFlipHint.setVisibility(state.isFlipped() ? View.GONE : View.VISIBLE);
        Badges.showLevel(binding.tvFlashLevel, card.deck().level());
        binding.tvFront.setText(word.term());
        showOrHide(binding.tvFrontReading, word.reading());
        Speaker.bind(binding.btnFlashSpeak, tts, word.term(), card.language());
        binding.tvBackTerm.setText(word.reading() == null ? word.term()
                : getString(R.string.study_term_reading, word.term(), word.reading()));
        binding.tvBackMeaning.setText(word.meaning());
        showOrHide(binding.tvBackPos, word.partOfSpeech());
        binding.llBackExample.setVisibility(word.example() == null ? View.GONE : View.VISIBLE);
        binding.tvBackExample.setText(word.example());
        showOrHide(binding.tvBackTranslation, word.exampleTranslation());
        binding.cardFlash.setContentDescription(state.isFlipped()
                ? getString(R.string.study_card_back, word.term(), word.meaning())
                : getString(R.string.study_card_front, word.term(), word.reading() == null ? "" : word.reading()));
    }

    private void openResult(SessionSummary summary) {
        NavHostFragment.findNavController(this).navigate(FlashcardFragmentDirections.actionFlashcardToResult(
                summary.kind(), summary.deckId(), summary.displayName(), summary.remembered(), summary.forgotten(),
                summary.streakBefore(), summary.streak(), summary.xp().xp(), summary.xp().isCapped(),
                toArray(summary.boxesBefore()), toArray(summary.boxesAfter())));
    }

    private static int[] toArray(java.util.List<Integer> values) {
        return values.stream().mapToInt(Integer::intValue).toArray();
    }

    private static void showOrHide(android.widget.TextView view, String text) {
        view.setText(text);
        view.setVisibility(text == null ? View.GONE : View.VISIBLE);
    }
}
