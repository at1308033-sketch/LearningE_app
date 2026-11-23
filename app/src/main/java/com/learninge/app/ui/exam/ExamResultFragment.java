package com.learninge.app.ui.exam;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.learninge.app.R;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.ExamAttempt;
import com.learninge.app.data.model.ExamQuestion;
import com.learninge.app.data.model.ExamSession;
import com.learninge.app.databinding.FragmentExamResultBinding;
import com.learninge.app.databinding.ItemExamAnswerBinding;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;
import com.learninge.app.ui.common.FeedbackBannerView;

/**
 * SCR-32 Kết quả thi thử (US-036).
 */
public class ExamResultFragment extends BaseFragment {

    public ExamResultFragment() {
        super(R.layout.fragment_exam_result);
    }

    @Override
    protected boolean hasDarkHeader() {
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentExamResultBinding binding = FragmentExamResultBinding.bind(view);
        padStatusBar(binding.headerExamResult);
        ExamResultFragmentArgs args = ExamResultFragmentArgs.fromBundle(requireArguments());
        binding.bannerExam.show(FeedbackBannerView.Kind.INFO, getString(R.string.exam_score_note));
        if (args.getIsTimeUp() && savedInstanceState == null) {
            Snackbar.make(view, R.string.exam_time_up, Snackbar.LENGTH_LONG).show();
        }
        NavController nav = NavHostFragment.findNavController(this);
        binding.btnExamDone.setOnClickListener(v -> nav.navigateUp());
        ExamResultViewModel viewModel = new ViewModelProvider(this).get(ExamResultViewModel.class);
        viewModel.session().observe(getViewLifecycleOwner(), result -> render(binding, nav, result));
        viewModel.load(args.getAttemptId());
    }

    private void render(FragmentExamResultBinding binding, NavController nav, Result<ExamSession> result) {
        if (!result.isOk()) {
            Snackbar.make(binding.getRoot(), ErrorMessages.of(requireContext(), result.error), Snackbar.LENGTH_LONG)
                    .show();
            return;
        }
        ExamAttempt attempt = result.value.attempt();
        binding.tvExamResultTest.setText(attempt.test().title());
        binding.tvExamScore.setText(String.valueOf(attempt.score()));
        binding.piExamScore.setProgressCompat(attempt.score(), true);
        binding.tvExamSummary.setText(getString(R.string.exam_summary, attempt.correct(), attempt.total(),
                attempt.durationSec() / 60, attempt.durationSec() % 60));
        binding.btnExamRetry.setOnClickListener(v -> nav.navigate(
                ExamResultFragmentDirections.actionResultToExam(attempt.test().code())));
        binding.llAnswers.removeAllViews();
        result.value.questions().forEach(question -> addAnswer(binding, question));
    }

    private void addAnswer(FragmentExamResultBinding binding, ExamQuestion question) {
        ItemExamAnswerBinding item = ItemExamAnswerBinding.inflate(LayoutInflater.from(binding.llAnswers.getContext()), binding.llAnswers, true);
        boolean isCorrect = question.isCorrect();
        item.ivAnswer.setImageResource(isCorrect ? R.drawable.ic_check : R.drawable.ic_x);
        item.ivAnswer.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                isCorrect ? R.color.success : R.color.error)));
        item.ivAnswer.setContentDescription(getString(isCorrect ? R.string.exam_correct : R.string.exam_wrong));
        item.tvAnswer.setText(question.selectedIndex() == null
                ? getString(R.string.exam_answer_none, question.number())
                : getString(isCorrect ? R.string.exam_answer_right : R.string.exam_answer_wrong, question.number(),
                question.options().get(question.selectedIndex())));
        item.tvAnswerQuestion.setText(question.question());
        item.tvAnswerCorrect.setText(getString(R.string.exam_correct_answer,
                question.options().get(question.correctIndex())));
        item.tvAnswerExplanation.setText(question.explanation());
        item.tvAnswerExplanation.setVisibility(question.explanation() == null ? View.GONE : View.VISIBLE);
        item.llAnswer.setOnClickListener(v -> {
            boolean isOpen = item.llDetail.getVisibility() == View.VISIBLE;
            item.llDetail.setVisibility(isOpen ? View.GONE : View.VISIBLE);
            item.ivAnswerChevron.setRotation(isOpen ? 0 : 90);
        });
    }
}
