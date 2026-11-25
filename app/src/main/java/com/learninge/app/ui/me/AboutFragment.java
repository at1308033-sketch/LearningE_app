package com.learninge.app.ui.me;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.BuildConfig;
import com.learninge.app.R;
import com.learninge.app.databinding.FragmentAboutBinding;
import com.learninge.app.ui.common.BaseFragment;

/**
 * SCR-24 Giới thiệu — nội dung tĩnh, không cần ViewModel.
 */
public class AboutFragment extends BaseFragment {

    public AboutFragment() {
        super(R.layout.fragment_about);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentAboutBinding binding = FragmentAboutBinding.bind(view);
        padStatusBar(binding.headerAbout);
        binding.toolbarAbout.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
        binding.tvVersion.setText(getString(R.string.about_version, BuildConfig.VERSION_NAME));
    }
}
