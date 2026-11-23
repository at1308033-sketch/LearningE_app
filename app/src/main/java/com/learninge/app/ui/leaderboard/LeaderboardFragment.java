package com.learninge.app.ui.leaderboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.data.model.Leaderboard;
import com.learninge.app.databinding.FragmentLeaderboardBinding;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;
import com.learninge.app.ui.common.TimeText;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;

/**
 * SCR-34 Bảng xếp hạng tuần (US-042).
 */
public class LeaderboardFragment extends BaseFragment {
    private static final long MINUTE_MILLIS = 60_000;
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM");

    private final Handler ticker = new Handler(Looper.getMainLooper());

    public LeaderboardFragment() {
        super(R.layout.fragment_leaderboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentLeaderboardBinding binding = FragmentLeaderboardBinding.bind(view);
        padStatusBar(binding.headerLeaderboard);
        binding.toolbarLeaderboard.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
        RankAdapter adapter = new RankAdapter();
        binding.rvRanks.setAdapter(adapter);
        LeaderboardViewModel viewModel = new ViewModelProvider(this).get(LeaderboardViewModel.class);
        viewModel.state().observe(getViewLifecycleOwner(), state -> render(binding, adapter, viewModel, state));
        viewModel.load();
    }

    @Override
    public void onDestroyView() {
        ticker.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }

    private void render(FragmentLeaderboardBinding binding, RankAdapter adapter, LeaderboardViewModel viewModel,
                        LeaderboardViewModel.State state) {
        binding.piLeaderboard.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);
        boolean hasBoard = state.board() != null;
        binding.groupBoard.setVisibility(hasBoard ? View.VISIBLE : View.GONE);
        binding.esLeaderboard.setVisibility(View.GONE);
        if (hasBoard) {
            renderBoard(binding, adapter, viewModel, state.board());
        } else if (!state.isLoading()) {
            showEmpty(binding, viewModel, state.error());
        }
        if (hasBoard && state.error() != null && state.error() != ErrorCode.NETWORK_OFFLINE) {
            Snackbar.make(binding.getRoot(), ErrorMessages.of(requireContext(), state.error()), Snackbar.LENGTH_LONG)
                    .setAction(R.string.common_retry, v -> viewModel.load()).show();
        }
    }

    private void showEmpty(FragmentLeaderboardBinding binding, LeaderboardViewModel viewModel, ErrorCode error) {
        binding.esLeaderboard.setVisibility(View.VISIBLE);
        binding.esLeaderboard.setTitle(error == ErrorCode.NETWORK_OFFLINE
                ? getString(R.string.leaderboard_need_network) : ErrorMessages.of(requireContext(), error));
        binding.esLeaderboard.setAction(R.string.common_retry, v -> viewModel.load());
    }

    private void renderBoard(FragmentLeaderboardBinding binding, RankAdapter adapter, LeaderboardViewModel viewModel,
                             Leaderboard board) {
        binding.toolbarLeaderboard.setSubtitle(getString(R.string.leaderboard_week,
                board.weekStart().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), DAY.format(board.weekStart()),
                DAY.format(board.weekStart().plusDays(6))));
        String time = TimeText.clock(requireContext(), board.fetchedAt());
        binding.tvUpdated.setText(getString(board.isCached() ? R.string.leaderboard_saved
                : R.string.leaderboard_updated, time));
        boolean isEmpty = board.entries().isEmpty();
        binding.esLeaderboard.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (isEmpty) {
            binding.esLeaderboard.setTitle(R.string.leaderboard_new_week);
        }
        adapter.submitList(board.entries());
        renderMe(binding, board);
        tick(binding, viewModel, board);
    }

    private void renderMe(FragmentLeaderboardBinding binding, Leaderboard board) {
        Leaderboard.Entry me = board.me();
        binding.tvMyRank.setText(me.rank() == 0 ? getString(R.string.leaderboard_no_rank_short)
                : String.valueOf(me.rank()));
        binding.ivMyAvatar.setImageResource(RankAdapter.avatar(me.nickname()));
        binding.tvMyTitle.setText(me.rank() == 0 ? getString(R.string.leaderboard_no_rank)
                : getString(R.string.leaderboard_my_rank, me.rank()));
        int missing = board.xpToNextRank();
        binding.tvMyNote.setVisibility(missing > 0 ? View.VISIBLE : View.GONE);
        binding.tvMyNote.setText(getString(R.string.leaderboard_to_next, missing, me.rank() - 1));
        binding.tvMyXp.setText(getString(R.string.xp, me.xp()));
    }

    // Đếm ngược cập nhật mỗi phút.
    private void tick(FragmentLeaderboardBinding binding, LeaderboardViewModel viewModel, Leaderboard board) {
        ticker.removeCallbacksAndMessages(null);
        Duration left = viewModel.untilWeekEnd(board);
        binding.tvCountdown.setText(getString(R.string.leaderboard_countdown, left.toDays(), left.toHours() % 24,
                left.toMinutes() % 60));
        ticker.postDelayed(() -> tick(binding, viewModel, board), MINUTE_MILLIS);
    }
}
