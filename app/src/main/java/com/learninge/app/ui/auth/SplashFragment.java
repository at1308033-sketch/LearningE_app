package com.learninge.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.R;
import com.learninge.app.databinding.FragmentSplashBinding;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.main.MainActivity;

/**
 * SCR-01: nạp seed lần đầu, kiểm tra phiên rồi vào app chính hoặc màn đăng nhập (FLOW-001, FLOW-010).
 */
public class SplashFragment extends BaseFragment {

    public SplashFragment() {
        super(R.layout.fragment_splash);
    }

    @Override
    protected boolean hasDarkHeader() {
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentSplashBinding binding = FragmentSplashBinding.bind(view);
        SplashViewModel viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        binding.btnSplashRetry.setOnClickListener(v -> viewModel.start(services()));
        viewModel.state().observe(getViewLifecycleOwner(), state -> render(binding, state));
        viewModel.start(services());
    }

    private void render(FragmentSplashBinding binding, SplashViewModel.State state) {
        boolean hasFailed = state == SplashViewModel.State.SEED_FAILED;
        binding.piSplash.setVisibility(hasFailed ? View.INVISIBLE : View.VISIBLE);
        binding.btnSplashRetry.setVisibility(hasFailed ? View.VISIBLE : View.GONE);
        binding.tvSplashStatus.setText(hasFailed ? R.string.splash_seed_failed : R.string.splash_loading);
        if (state == SplashViewModel.State.SIGNED_IN) {
            startActivity(new Intent(requireContext(), MainActivity.class));
            requireActivity().finish();
        } else if (state == SplashViewModel.State.SIGNED_OUT) {
            NavHostFragment.findNavController(this)
                    .navigate(SplashFragmentDirections.actionSplashToLogin());
        }
    }
}
