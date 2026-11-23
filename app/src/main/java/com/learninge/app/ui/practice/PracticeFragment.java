package com.learninge.app.ui.practice;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.NavMainDirections;
import com.learninge.app.R;
import com.learninge.app.databinding.FragmentPracticeBinding;
import com.learninge.app.ui.common.BaseFragment;

/**
 * SCR-12 Luyện tập. Phần quiz làm ở T-040/T-041; hiện có lối vào Luyện thi.
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
    }
}
