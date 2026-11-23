package com.learninge.app.ui.me;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.learninge.app.R;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.Statistics;
import com.learninge.app.data.model.Statistics.DayActivity;
import com.learninge.app.databinding.FragmentProgressBinding;
import com.learninge.app.ui.common.BarChartView;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * SCR-21 Tiến độ (US-017).
 */
public class ProgressFragment extends BaseFragment {

    public ProgressFragment() {
        super(R.layout.fragment_progress);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentProgressBinding binding = FragmentProgressBinding.bind(view);
        padStatusBar(binding.headerProgress);
        binding.toolbarProgress.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        TopicProgressAdapter adapter = new TopicProgressAdapter();
        binding.rvTopicProgress.setAdapter(adapter);
        ProgressViewModel viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);
        viewModel.statistics().observe(getViewLifecycleOwner(), result -> render(binding, adapter, viewModel, result));
        viewModel.load();
    }

    private void render(FragmentProgressBinding binding, TopicProgressAdapter adapter, ProgressViewModel viewModel,
                        Result<Statistics> result) {
        if (!result.isOk()) {
            Snackbar.make(binding.getRoot(), ErrorMessages.of(requireContext(), result.error), Snackbar.LENGTH_LONG)
                    .setAction(R.string.common_retry, v -> viewModel.load()).show();
            return;
        }
        Statistics statistics = result.value;
        binding.tvStreakCount.setText(String.valueOf(statistics.streak()));
        binding.tvStreakRecord.setText(getString(R.string.progress_record, statistics.longestStreak()));
        binding.tvStreakNote.setVisibility(statistics.streak() == 0 ? View.VISIBLE : View.GONE);
        binding.tvMastered.setText(String.valueOf(statistics.mastered()));
        binding.tvLearning.setText(String.valueOf(statistics.learning()));
        binding.tvNew.setText(String.valueOf(statistics.newWords()));
        binding.tvMasteredOf.setText(getString(R.string.progress_mastered_of, statistics.mastered(),
                statistics.totalWords()));
        binding.tvQuizAverage.setText(statistics.averageQuizScore() == null ? getString(R.string.progress_no_quiz)
                : getString(R.string.progress_quiz_average, statistics.averageQuizScore()));
        binding.chartWeek.setBars(bars(statistics.week()));
        adapter.submitList(statistics.topics());
    }

    private List<BarChartView.Bar> bars(List<DayActivity> week) {
        // Thứ Hai = 1 … Chủ nhật = 7, trùng thứ tự mảng nhãn.
        String[] labels = getResources().getStringArray(R.array.weekdays_short);
        List<BarChartView.Bar> bars = new ArrayList<>();
        for (DayActivity day : week) {
            bars.add(new BarChartView.Bar(labels[day.day().getDayOfWeek().getValue() - 1], day.count()));
        }
        return bars;
    }
}
