package com.learninge.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.R;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.main.MainActivity;

/**
 * SCR-01: kiểm tra phiên rồi vào app chính hoặc màn đăng nhập (FLOW-001).
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
        SplashViewModel viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        viewModel.start(services());
        viewModel.signedIn().observe(getViewLifecycleOwner(), this::leave);
    }

    private void leave(boolean signedIn) {
        if (signedIn) {
            startActivity(new Intent(requireContext(), MainActivity.class));
            requireActivity().finish();
        } else {
            NavHostFragment.findNavController(this)
                    .navigate(SplashFragmentDirections.actionSplashToLogin());
        }
    }
}
