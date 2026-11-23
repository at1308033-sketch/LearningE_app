package com.learninge.app.ui.me;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.databinding.FragmentSyncBinding;
import com.learninge.app.sync.SyncView;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;
import com.learninge.app.ui.common.FeedbackBannerView;
import com.learninge.app.ui.common.TimeText;

/**
 * SCR-35 Trạng thái đồng bộ (US-041).
 */
public class SyncFragment extends BaseFragment {

    private record Look(@DrawableRes int icon, @ColorRes int background, @ColorRes int foreground,
                        @StringRes int title, @StringRes int message) {
    }

    public SyncFragment() {
        super(R.layout.fragment_sync);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentSyncBinding binding = FragmentSyncBinding.bind(view);
        padStatusBar(binding.headerSync);
        binding.toolbarSync.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.bannerSync.show(FeedbackBannerView.Kind.INFO, getString(R.string.sync_safe_note));
        SyncViewModel viewModel = new ViewModelProvider(this).get(SyncViewModel.class);
        binding.btnSync.setOnClickListener(v -> viewModel.syncNow());
        viewModel.state().observe(getViewLifecycleOwner(), state -> render(binding, state));
        viewModel.syncStatus().observe(getViewLifecycleOwner(), status -> viewModel.refresh());
        viewModel.online().observe(getViewLifecycleOwner(), online -> viewModel.refresh());
    }

    private void render(FragmentSyncBinding binding, SyncViewModel.State state) {
        Look look = look(state.view());
        binding.ivSync.setImageResource(look.icon());
        binding.ivSync.setBackgroundTintList(color(look.background()));
        binding.ivSync.setImageTintList(color(look.foreground()));
        binding.tvSyncTitle.setText(look.title());
        boolean isSyncing = state.view() == SyncView.SYNCING;
        binding.tvSyncMessage.setText(isSyncing && state.status().total() > 0
                ? getString(R.string.sync_progress, state.status().pushed(), state.status().total())
                : getString(look.message()));
        binding.piSync.setVisibility(isSyncing ? View.VISIBLE : View.GONE);
        binding.tvLastSync.setText(state.sync() == null || state.sync().lastSyncAt() == null
                ? getString(R.string.sync_never) : TimeText.clock(requireContext(), state.sync().lastSyncAt()));
        binding.tvPending.setText(String.valueOf(state.pending()));
        binding.tvLastError.setText(errorText(state));
        binding.tvLastError.setTextColor(ContextCompat.getColor(requireContext(),
                state.view() == SyncView.ERROR ? R.color.error : R.color.on_surface));
        binding.rowServer.setVisibility(state.server() == null ? View.GONE : View.VISIBLE);
        binding.tvServer.setText(state.server());
        binding.btnSync.setEnabled(!isSyncing && state.view() != SyncView.OFFLINE);
        binding.btnSync.setText(isSyncing ? R.string.sync_running
                : state.view() == SyncView.ERROR ? R.string.common_retry : R.string.sync_now);
        binding.tvSyncDisabled.setVisibility(state.view() == SyncView.OFFLINE ? View.VISIBLE : View.GONE);
    }

    private String errorText(SyncViewModel.State state) {
        if (state.sync() == null || state.sync().lastErrorCode() == null) {
            return getString(R.string.sync_no_error);
        }
        try {
            return ErrorMessages.of(requireContext(), ErrorCode.valueOf(state.sync().lastErrorCode()));
        } catch (IllegalArgumentException serverRecordCode) {
            // Mã lỗi từng bản ghi của máy chủ (INVALID_RECORD, DECK_NOT_OWNED…).
            return getString(R.string.sync_record_rejected, state.sync().lastErrorCode());
        }
    }

    private static Look look(SyncView view) {
        return switch (view) {
            case SYNCING -> new Look(R.drawable.ic_refresh, R.color.primary_container, R.color.on_primary_container,
                    R.string.sync_running, R.string.sync_running_note);
            case OFFLINE -> new Look(R.drawable.ic_cloud_off, R.color.surface_variant, R.color.on_surface_variant,
                    R.string.sync_offline, R.string.sync_offline_note);
            case ERROR -> new Look(R.drawable.ic_info, R.color.error_container, R.color.error, R.string.sync_error,
                    R.string.sync_error_note);
            case PENDING -> new Look(R.drawable.ic_cloud, R.color.primary_container, R.color.on_primary_container,
                    R.string.sync_pending, R.string.sync_pending_note);
            case SYNCED -> new Look(R.drawable.ic_cloud_check, R.color.success_container, R.color.success,
                    R.string.sync_done, R.string.sync_done_note);
        };
    }

    private ColorStateList color(@ColorRes int color) {
        return ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color));
    }
}
