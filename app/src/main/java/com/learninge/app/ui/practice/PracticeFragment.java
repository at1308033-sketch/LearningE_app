package com.learninge.app.ui.practice;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.NavMainDirections;
import com.learninge.app.R;
import com.learninge.app.databinding.FragmentPracticeBinding;
import com.learninge.app.ui.common.BaseFragment;

/**
 * SCR-12 Luyện tập: lối vào Luyện thi và chọn chủ đề để quiz.
 */
public class PracticeFragment extends BaseFragment {

    public PracticeFragment() {
        super(R.layout.fragment_practice);
    }

    @Override
    protected boolean hasDarkHeader() {
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentPracticeBinding binding = FragmentPracticeBinding.bind(view);
        padStatusBar(binding.headerPractice);

        binding.cardExam.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(NavMainDirections.actionGlobalExamHub()));

        QuizDeckAdapter adapter = new QuizDeckAdapter(item ->
                NavHostFragment.findNavController(this).navigate(
                        NavMainDirections.actionGlobalQuiz(item.deck().id(), item.deck().name())));
        binding.rvQuizTopics.setLayoutManager(
                new androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2));
        binding.rvQuizTopics.setAdapter(adapter);

        PracticeViewModel viewModel = new ViewModelProvider(this).get(PracticeViewModel.class);
        viewModel.topics().observe(getViewLifecycleOwner(), result -> {
            if (result != null && result.isOk()) {
                adapter.submitList(result.value);
            }
        });
        viewModel.load();
    }
}
