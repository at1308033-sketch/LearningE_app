package com.learninge.app.ui.me;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.data.model.DeviceSession;
import com.learninge.app.databinding.FragmentSessionsBinding;
import com.learninge.app.databinding.ItemSessionBinding;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;
import com.learninge.app.ui.common.FeedbackBannerView;
import com.learninge.app.ui.common.TimeText;

/**
 * SCR-36 Thiết bị đăng nhập: xem và đăng xuất từ xa các máy khác (EP-17..EP-19).
 */
public class SessionsFragment extends BaseFragment {

    public SessionsFragment() {
        super(R.layout.fragment_sessions);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentSessionsBinding binding = FragmentSessionsBinding.bind(view);
        padStatusBar(binding.headerSessions);
        binding.toolbarSessions.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
        SessionsViewModel viewModel = new ViewModelProvider(this).get(SessionsViewModel.class);
        binding.btnRevokeOthers.setOnClickListener(v -> confirm(getString(R.string.sessions_revoke_others_title),
                viewModel::revokeOthers));
        viewModel.state().observe(getViewLifecycleOwner(), state -> render(binding, viewModel, state));
        viewModel.load();
    }

    private void render(FragmentSessionsBinding binding, SessionsViewModel viewModel, SessionsViewModel.State state) {
        binding.piSessions.setVisibility(state.isBusy() ? View.VISIBLE : View.INVISIBLE);
        boolean hasList = state.sessions() != null;
        binding.groupSessions.setVisibility(hasList ? View.VISIBLE : View.GONE);
        binding.esSessions.setVisibility(!hasList && state.error() != null ? View.VISIBLE : View.GONE);
        if (hasList) {
            renderList(binding, viewModel, state);
        } else if (state.error() != null) {
            binding.esSessions.setTitle(state.error() == ErrorCode.NETWORK_OFFLINE
                    ? getString(R.string.sessions_need_network) : ErrorMessages.of(requireContext(), state.error()));
            binding.esSessions.setAction(R.string.common_retry, v -> viewModel.load());
        }
    }

    private void renderList(FragmentSessionsBinding binding, SessionsViewModel viewModel,
                            SessionsViewModel.State state) {
        binding.llSessions.removeAllViews();
        boolean hasOthers = false;
        for (DeviceSession session : state.sessions()) {
            add(binding, viewModel, session, !state.isBusy());
            hasOthers |= !session.isCurrent();
        }
        binding.btnRevokeOthers.setVisibility(hasOthers ? View.VISIBLE : View.GONE);
        binding.btnRevokeOthers.setEnabled(!state.isBusy());
        if (state.error() != null) {
            binding.bannerSessions.show(FeedbackBannerView.Kind.ERROR,
                    ErrorMessages.of(requireContext(), state.error()));
        } else {
            binding.bannerSessions.show(FeedbackBannerView.Kind.INFO,
                    getString(hasOthers ? R.string.sessions_note : R.string.sessions_only_this));
        }
    }

    private void add(FragmentSessionsBinding binding, SessionsViewModel viewModel, DeviceSession session,
                     boolean isEnabled) {
        ItemSessionBinding row = ItemSessionBinding.inflate(LayoutInflater.from(requireContext()),
                binding.llSessions, true);
        String name = session.deviceName().isEmpty() ? getString(R.string.sessions_unnamed) : session.deviceName();
        row.tvSession.setText(name);
        row.tvSessionNote.setText(note(session));
        row.tvSessionCurrent.setVisibility(session.isCurrent() ? View.VISIBLE : View.GONE);
        row.btnRevoke.setVisibility(session.isCurrent() ? View.GONE : View.VISIBLE);
        row.btnRevoke.setEnabled(isEnabled);
        row.btnRevoke.setContentDescription(getString(R.string.sessions_revoke_device, name));
        row.btnRevoke.setOnClickListener(v -> confirm(getString(R.string.sessions_revoke_title, name),
                () -> viewModel.revoke(session.id())));
    }

    private String note(DeviceSession session) {
        String signedIn = TimeText.clock(requireContext(), session.createdAt());
        return session.lastUsedAt() == null ? getString(R.string.sessions_signed_in, signedIn)
                : getString(R.string.sessions_signed_in_used, signedIn,
                TimeText.day(requireContext(), session.lastUsedAt()));
    }

    private void confirm(String title, Runnable action) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(R.string.sessions_revoke_message)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.sessions_revoke, (dialog, which) -> action.run())
                .show();
    }
}
