package com.learninge.app.ui.exam;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.learninge.app.R;
import com.learninge.app.data.model.ExamQuestion;
import com.learninge.app.data.model.ExamSession;
import com.learninge.app.databinding.FragmentExamBinding;
import com.learninge.app.databinding.ItemOptionBinding;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;

import java.util.Locale;

/**
 * SCR-31 Làm bài thi thử (US-036). Đồng hồ tính từ lúc bắt đầu nên thoát app không dừng giờ.
 */
public class ExamFragment extends BaseFragment {
    private static final String[] LETTERS = {"A", "B", "C", "D"};
    private static final long WARNING_SECONDS = 120;
    private static final long ANNOUNCE_EVERY_SECONDS = 300;
    private static final long LAST_ANNOUNCE_SECONDS = 60;

    private CountDownTimer timer;

    public ExamFragment() {
        super(R.layout.fragment_exam);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentExamBinding binding = FragmentExamBinding.bind(view);
        padStatusBar(binding.headerExam);
        ExamViewModel viewModel = new ViewModelProvider(this).get(ExamViewModel.class);
        binding.btnExamClose.setOnClickListener(v -> confirmExit());
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        confirmExit();
                    }
                });
        binding.btnPrevious.setOnClickListener(v -> viewModel.goTo(index(viewModel) - 1));
        binding.btnNext.setOnClickListener(v -> viewModel.goTo(index(viewModel) + 1));
        binding.btnFlag.setOnClickListener(v -> viewModel.toggleFlag());
        binding.btnGrid.setOnClickListener(v -> showGrid(viewModel));
        viewModel.state().observe(getViewLifecycleOwner(), state -> render(binding, viewModel, state));
        viewModel.finished().observe(getViewLifecycleOwner(), this::openResult);
        viewModel.start(ExamFragmentArgs.fromBundle(requireArguments()).getTestCode());
    }

    @Override
    public void onDestroyView() {
        stopTimer();
        super.onDestroyView();
    }

    private static int index(ExamViewModel viewModel) {
        ExamViewModel.State state = viewModel.state().getValue();
        return state == null ? 0 : state.index();
    }

    private void render(FragmentExamBinding binding, ExamViewModel viewModel, ExamViewModel.State state) {
        if (state.error() != null) {
            Snackbar.make(binding.getRoot(), ErrorMessages.of(requireContext(), state.error()),
                    Snackbar.LENGTH_LONG).show();
        }
        if (state.session() == null) {
            return;
        }
        if (timer == null) {
            startTimer(binding, viewModel);
        }
        ExamSession session = state.session();
        ExamQuestion question = state.question();
        int total = session.questions().size();
        binding.tvExamTitle.setText(session.attempt().test().title());
        binding.tvExamPosition.setText(getString(R.string.exam_position, question.number(), total));
        binding.tvExamDone.setText(getString(R.string.exam_done, session.answered(), session.flagged()));
        binding.piExam.setMax(total);
        binding.piExam.setProgress(question.number());
        binding.tvPassage.setVisibility(question.passage() == null ? View.GONE : View.VISIBLE);
        binding.tvPassage.setText(question.passage());
        binding.tvQuestionNumber.setText(getString(R.string.exam_question, question.number()));
        binding.tvQuestion.setText(question.question());
        renderOptions(binding, viewModel, question);
        binding.btnPrevious.setEnabled(state.index() > 0);
        boolean isLast = state.index() == total - 1;
        binding.btnNext.setText(isLast ? R.string.exam_submit : R.string.exam_next);
        binding.btnNext.setOnClickListener(v -> {
            if (isLast) {
                confirmSubmit(viewModel, session);
            } else {
                viewModel.goTo(state.index() + 1);
            }
        });
        binding.btnFlag.setText(question.isFlagged() ? R.string.exam_unflag : R.string.exam_flag);
    }

    private void renderOptions(FragmentExamBinding binding, ExamViewModel viewModel, ExamQuestion question) {
        binding.llOptions.removeAllViews();
        for (int i = 0; i < question.options().size(); i++) {
            ItemOptionBinding item = ItemOptionBinding.inflate(LayoutInflater.from(binding.llOptions.getContext()), binding.llOptions, true);
            int option = i;
            boolean isSelected = question.selectedIndex() != null && question.selectedIndex() == i;
            item.tvLetter.setText(LETTERS[i]);
            item.tvOption.setText(question.options().get(i));
            item.ivOptionCheck.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
            item.cardOption.setChecked(isSelected);
            item.cardOption.setStrokeColor(ContextCompat.getColor(requireContext(),
                    isSelected ? R.color.feat_exam : R.color.outline_strong));
            item.cardOption.setStrokeWidth(getResources().getDimensionPixelSize(
                    isSelected ? R.dimen.stroke_selected : R.dimen.stroke_card));
            item.cardOption.setCardBackgroundColor(ContextCompat.getColor(requireContext(),
                    isSelected ? R.color.feat_exam_container : R.color.surface));
            item.tvLetter.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                    isSelected ? R.color.feat_exam : R.color.surface_variant)));
            item.tvLetter.setTextColor(ContextCompat.getColor(requireContext(),
                    isSelected ? R.color.on_feat_exam : R.color.on_surface));
            item.cardOption.setOnClickListener(v -> viewModel.select(option));
        }
    }

    private void showGrid(ExamViewModel viewModel) {
        ExamViewModel.State state = viewModel.state().getValue();
        if (state != null && state.session() != null) {
            QuestionGridSheet.show(requireContext(), state.session(), state.index(), viewModel::goTo,
                    () -> confirmSubmit(viewModel, state.session()));
        }
    }

    private void confirmSubmit(ExamViewModel viewModel, ExamSession session) {
        int left = session.questions().size() - session.answered();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(left == 0 ? getString(R.string.exam_submit_title)
                        : getString(R.string.exam_submit_left, left))
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.exam_submit, (dialog, which) -> viewModel.submit(false))
                .show();
    }

    private void confirmExit() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.exam_exit_title)
                .setMessage(R.string.exam_exit_message)
                .setNegativeButton(R.string.exam_exit_stay, null)
                .setPositiveButton(R.string.exam_exit_leave, (dialog, which) ->
                        NavHostFragment.findNavController(this).navigateUp())
                .show();
    }

    private void startTimer(FragmentExamBinding binding, ExamViewModel viewModel) {
        timer = new CountDownTimer(viewModel.remainingMillis(), 1_000) {
            @Override
            public void onTick(long millisLeft) {
                showTime(binding, (millisLeft + 999) / 1_000);
            }

            @Override
            public void onFinish() {
                showTime(binding, 0);
                viewModel.submit(true);
            }
        }.start();
    }

    private void showTime(FragmentExamBinding binding, long seconds) {
        boolean isWarning = seconds < WARNING_SECONDS;
        binding.tvTimer.setText(String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60));
        binding.tvTimer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                isWarning ? R.color.error_container : R.color.feat_exam_container)));
        binding.tvTimer.setTextColor(ContextCompat.getColor(requireContext(),
                isWarning ? R.color.error : R.color.feat_exam));
        // TalkBack chỉ đọc mỗi 5 phút và lúc còn 1 phút.
        if (seconds > 0 && (seconds % ANNOUNCE_EVERY_SECONDS == 0 || seconds == LAST_ANNOUNCE_SECONDS)) {
            binding.tvTimerAnnounce.setText(getString(R.string.exam_time_left, seconds / 60));
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void openResult(ExamViewModel.Finished finished) {
        stopTimer();
        NavHostFragment.findNavController(this).navigate(ExamFragmentDirections.actionExamToResult(
                finished.attemptId()).setIsTimeUp(finished.isTimeUp()));
    }
}
