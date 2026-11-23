package com.learninge.app.ui.me;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.NavMainDirections;
import com.learninge.app.R;
import com.learninge.app.databinding.FragmentMeBinding;
import com.learninge.app.ui.common.BaseFragment;

/**
 * SCR-22 Tôi. Hồ sơ và cài đặt làm ở T-024; hiện có lối vào các màn tiến độ.
 */
public class MeFragment extends BaseFragment {

    public MeFragment() {
        super(R.layout.fragment_me);
    }

    @Override
    protected boolean hasDarkHeader() {
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentMeBinding binding = FragmentMeBinding.bind(view);
        padStatusBar(binding.headerMe);
        binding.rowProgress.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(NavMainDirections.actionGlobalProgress()));
        binding.rowSync.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(NavMainDirections.actionGlobalSync()));
        binding.rowLeaderboard.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(NavMainDirections.actionGlobalLeaderboard()));
    }
}
