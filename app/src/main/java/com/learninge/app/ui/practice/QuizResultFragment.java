package com.learninge.app.ui.practice;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.R;
import com.learninge.app.databinding.FragmentQuizResultBinding;
import com.learninge.app.ui.common.BaseFragment;

/**
 * SCR-14 Kết quả quiz chủ đề.
 */
public class QuizResultFragment extends BaseFragment {

    public QuizResultFragment() {
        super(R.layout.fragment_quiz_result);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentQuizResultBinding binding = FragmentQuizResultBinding.bind(view);
        QuizResultFragmentArgs args = QuizResultFragmentArgs.fromBundle(requireArguments());

        int correct = args.getCorrect();
        int total = args.getTotal();
        int pct = total > 0 ? (correct * 100 / total) : 0;

        binding.tvQuizResultTitle.setText(args.getDeckName());
        binding.tvQuizResultScore.setText(getString(R.string.quiz_result_score, correct, total));
        binding.tvQuizResultPct.setText(getString(R.string.result_percent, pct));
        binding.piQuizResult.setMax(Math.max(1, total));
        binding.piQuizResult.setProgress(correct);

        binding.btnQuizRetry.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(
                        QuizResultFragmentDirections.actionQuizResultToQuiz(args.getDeckId(), args.getDeckName())));

        binding.btnQuizBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.practice_graph));
    }
}
