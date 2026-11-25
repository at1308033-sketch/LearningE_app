package com.learninge.app.ui.practice;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.learninge.app.R;
import com.learninge.app.databinding.FragmentQuizBinding;
import com.learninge.app.databinding.ItemOptionBinding;
import com.learninge.app.ui.common.BaseFragment;

/**
 * SCR-13 Làm quiz chủ đề.
 */
public class QuizFragment extends BaseFragment {
    private static final String[] LETTERS = {"A", "B", "C", "D"};

    public QuizFragment() {
        super(R.layout.fragment_quiz);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentQuizBinding binding = FragmentQuizBinding.bind(view);
        QuizFragmentArgs args = QuizFragmentArgs.fromBundle(requireArguments());
        QuizViewModel viewModel = new ViewModelProvider(this).get(QuizViewModel.class);

        binding.btnQuizClose.setOnClickListener(v -> confirmExit());
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        confirmExit();
                    }
                });

        viewModel.state().observe(getViewLifecycleOwner(), state -> render(binding, viewModel, state));
        viewModel.finished().observe(getViewLifecycleOwner(), fin -> {
            NavHostFragment.findNavController(this).navigate(
                    QuizFragmentDirections.actionQuizToResult(
                            args.getDeckId(), fin.deckName(), fin.correct(), fin.total()));
        });

        viewModel.start(args.getDeckId(), args.getDeckName());
    }

    private void render(FragmentQuizBinding binding, QuizViewModel viewModel, QuizViewModel.State state) {
        QuizViewModel.Question q = state.question();
        binding.tvQuizTerm.setText(q.term());
        boolean hasReading = q.reading() != null && !q.reading().isBlank();
        binding.tvQuizReading.setVisibility(hasReading ? View.VISIBLE : View.GONE);
        binding.tvQuizReading.setText(hasReading ? q.reading() : null);

        int progress = state.index() + 1;
        binding.piQuiz.setMax(state.total());
        binding.piQuiz.setProgress(progress);
        binding.tvQuizPosition.setText(getString(R.string.study_position, progress, state.total()));

        binding.llQuizOptions.removeAllViews();
        for (int i = 0; i < q.options().size(); i++) {
            ItemOptionBinding item = ItemOptionBinding.inflate(
                    LayoutInflater.from(binding.llQuizOptions.getContext()), binding.llQuizOptions, true);
            final int idx = i;
            item.tvLetter.setText(LETTERS[i]);
            item.tvOption.setText(q.options().get(i));

            if (state.selectedIndex() != null) {
                // Show result colors
                boolean isCorrect = i == q.correctIndex();
                boolean isSelected = state.selectedIndex() == i;
                int cardColor, strokeColor, letterColor, letterBg;
                if (isCorrect) {
                    cardColor = R.color.success_container;
                    strokeColor = R.color.success;
                    letterBg = R.color.success;
                    letterColor = R.color.on_hero;
                } else if (isSelected) {
                    cardColor = R.color.error_container;
                    strokeColor = R.color.error;
                    letterBg = R.color.error;
                    letterColor = R.color.on_hero;
                } else {
                    cardColor = R.color.surface;
                    strokeColor = R.color.outline_strong;
                    letterBg = R.color.surface_variant;
                    letterColor = R.color.on_surface;
                }
                item.cardOption.setCardBackgroundColor(ContextCompat.getColor(requireContext(), cardColor));
                item.cardOption.setStrokeColor(ContextCompat.getColor(requireContext(), strokeColor));
                item.cardOption.setStrokeWidth(getResources().getDimensionPixelSize(
                        isCorrect || isSelected ? R.dimen.stroke_selected : R.dimen.stroke_card));
                item.tvLetter.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), letterBg)));
                item.tvLetter.setTextColor(ContextCompat.getColor(requireContext(), letterColor));
                item.ivOptionCheck.setVisibility(isCorrect ? View.VISIBLE : View.INVISIBLE);
                item.cardOption.setClickable(false);
            } else {
                // Normal unselected state
                item.cardOption.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface));
                item.cardOption.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.outline_strong));
                item.cardOption.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.stroke_card));
                item.tvLetter.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.surface_variant)));
                item.tvLetter.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface));
                item.ivOptionCheck.setVisibility(View.INVISIBLE);
                item.cardOption.setOnClickListener(v -> viewModel.select(idx));
            }
        }
    }

    private void confirmExit() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.quiz_exit_title)
                .setMessage(R.string.quiz_exit_message)
                .setNegativeButton(R.string.exam_exit_stay, null)
                .setPositiveButton(R.string.exam_exit_leave, (d, w) ->
                        NavHostFragment.findNavController(this).navigateUp())
                .show();
    }
}
