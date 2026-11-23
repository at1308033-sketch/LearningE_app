package com.learninge.app.ui.exam;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.NavMainDirections;
import com.learninge.app.R;
import com.learninge.app.data.model.ExamAttempt;
import com.learninge.app.databinding.FragmentExamHistoryBinding;
import com.learninge.app.databinding.ItemExamRowBinding;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;
import com.learninge.app.ui.common.TimeText;

/**
 * Lịch sử thi thử, mở từ SCR-30 (US-036).
 */
public class ExamHistoryFragment extends BaseFragment {

    public ExamHistoryFragment() {
        super(R.layout.fragment_exam_history);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentExamHistoryBinding binding = FragmentExamHistoryBinding.bind(view);
        padStatusBar(binding.headerHistory);
        NavController nav = NavHostFragment.findNavController(this);
        binding.toolbarHistory.setNavigationOnClickListener(v -> nav.navigateUp());
        ExamHistoryViewModel viewModel = new ViewModelProvider(this).get(ExamHistoryViewModel.class);
        viewModel.attempts().observe(getViewLifecycleOwner(), result -> {
            binding.llHistory.removeAllViews();
            boolean isEmpty = !result.isOk() || result.value.isEmpty();
            binding.esHistory.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            if (result.isOk()) {
                result.value.forEach(attempt -> add(binding.llHistory, nav, attempt));
            } else {
                binding.esHistory.setTitle(ErrorMessages.of(requireContext(), result.error));
            }
        });
        viewModel.load();
    }

    private void add(ViewGroup parent, NavController nav, ExamAttempt attempt) {
        ItemExamRowBinding row = ItemExamRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, true);
        row.ivRow.setImageResource(R.drawable.ic_history);
        row.tvRowTitle.setText(attempt.test().title());
        row.tvRowNote.setText(getString(R.string.exam_history_row, attempt.score(), attempt.correct(),
                attempt.total(), TimeText.clock(requireContext(), attempt.submittedAt())));
        row.btnRow.setVisibility(View.GONE);
        row.getRoot().setOnClickListener(v ->
                nav.navigate(NavMainDirections.actionGlobalExamResult(attempt.id())));
    }
}
