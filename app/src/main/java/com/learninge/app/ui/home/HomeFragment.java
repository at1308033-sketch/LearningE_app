package com.learninge.app.ui.home;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.learninge.app.NavMainDirections;
import com.learninge.app.R;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.data.model.TodaySummary;
import com.learninge.app.databinding.FragmentHomeBinding;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;
import com.learninge.app.ui.common.TopicArt;
import com.learninge.app.ui.study.StudySource;

/**
 * SCR-05 Trang chủ (US-016): một hành động chính là "Bắt đầu học".
 */
public class HomeFragment extends BaseFragment {
    private static final int NOON = 12;
    private static final int EVENING = 18;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentHomeBinding binding = FragmentHomeBinding.bind(view);
        padStatusBar(binding.llHome);
        HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.today().observe(getViewLifecycleOwner(), result -> render(binding, viewModel, result));
        viewModel.weekRank().observe(getViewLifecycleOwner(), rank -> binding.tvHomeRank.setText(rank.rank() == 0
                ? getString(R.string.home_week_xp, rank.xp()) : getString(R.string.home_rank, rank.rank(), rank.xp())));
        binding.cardLeaderboard.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(NavMainDirections.actionGlobalLeaderboard()));
        // Dữ liệu vừa kéo về thì hiện ngay; trạng thái đầu tiên luôn là "không chạy" nên cũng là lần tải đầu.
        viewModel.syncStatus().observe(getViewLifecycleOwner(), status -> {
            if (!status.isRunning()) {
                viewModel.load();
            }
        });
    }

    private void render(FragmentHomeBinding binding, HomeViewModel viewModel, Result<TodaySummary> result) {
        if (!result.isOk()) {
            Snackbar.make(binding.getRoot(), ErrorMessages.of(requireContext(), result.error), Snackbar.LENGTH_LONG)
                    .setAction(R.string.common_retry, v -> viewModel.load()).show();
            return;
        }
        TodaySummary today = result.value;
        int hour = viewModel.hour();
        binding.tvGreeting.setText(hour < NOON ? R.string.home_morning
                : hour < EVENING ? R.string.home_afternoon : R.string.home_evening);
        binding.tvName.setText(today.displayName());
        binding.ivStreak.setImageResource(today.streak() > 0 ? R.drawable.px_flame_badge
                : R.drawable.px_flame_badge_off);
        binding.tvStreak.setText(getResources().getQuantityString(R.plurals.days, today.streak(), today.streak()));
        renderSession(binding, today);
        renderLastDeck(binding, today.lastDeck());
    }

    private void renderSession(FragmentHomeBinding binding, TodaySummary today) {
        boolean hasCards = today.cards() > 0;
        binding.tvCards.setText(String.valueOf(today.cards()));
        binding.tvMinutes.setText(hasCards ? getString(R.string.home_minutes, today.estimatedMinutes())
                : getString(R.string.home_nothing_due));
        binding.tvDue.setText(getString(R.string.home_due, today.due()));
        binding.tvNewWords.setText(getString(R.string.home_new_words, today.newWords()));
        binding.tvGoal.setText(getString(R.string.home_goal, today.learnedToday(), today.dailyGoal()));
        binding.piGoal.setMax(today.dailyGoal());
        binding.piGoal.setProgress(Math.min(today.learnedToday(), today.dailyGoal()));
        binding.tvGoalNote.setText(today.hasMetGoal() ? R.string.home_goal_met : R.string.home_goal_not_met);
        binding.btnStart.setOnClickListener(v -> start(today));
    }

    private void start(TodaySummary today) {
        NavController nav = NavHostFragment.findNavController(this);
        if (today.due() > 0) {
            nav.navigate(NavMainDirections.actionGlobalFlashcard(StudySource.REVIEW));
        } else if (today.lastDeck() != null && today.lastDeck().newWords() > 0) {
            nav.navigate(NavMainDirections.actionGlobalFlashcard(StudySource.NEW)
                    .setDeckId(today.lastDeck().deck().id()));
        } else {
            openLearnTab();
        }
    }

    private void renderLastDeck(FragmentHomeBinding binding, DeckSummary deck) {
        binding.cardFirstTopic.setVisibility(deck == null ? View.VISIBLE : View.GONE);
        binding.cardLastDeck.setVisibility(deck == null ? View.GONE : View.VISIBLE);
        binding.tvContinueLabel.setText(deck == null ? R.string.home_first_label : R.string.home_continue_label);
        binding.cardFirstTopic.setOnClickListener(v -> openLearnTab());
        if (deck == null) {
            return;
        }
        binding.ivLastDeck.setImageResource(TopicArt.icon(deck.deck()));
        binding.ivLastDeck.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), TopicArt.tile(deck.deck()))));
        binding.tvLastDeck.setText(deck.deck().name());
        binding.tvLastDeckCount.setText(getString(R.string.home_deck_count, deck.mastered(), deck.total()));
        binding.piLastDeck.setMax(Math.max(1, deck.total()));
        binding.piLastDeck.setProgress(deck.mastered());
        binding.cardLastDeck.setOnClickListener(v -> openDeck(deck));
    }

    private void openDeck(DeckSummary deck) {
        NavHostFragment.findNavController(this).navigate(NavMainDirections.actionGlobalWordList(deck.deck().id()));
    }

    private void openLearnTab() {
        BottomNavigationView tabs = requireActivity().findViewById(R.id.bottom_nav);
        NavigationUI.onNavDestinationSelected(tabs.getMenu().findItem(R.id.learn_graph),
                NavHostFragment.findNavController(this));
    }
}
