package com.learninge.app.ui.study;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.NavMainDirections;
import com.learninge.app.R;
import com.learninge.app.data.model.ActivityType;
import com.learninge.app.data.model.SessionSummary;
import com.learninge.app.data.model.XpGain;
import com.learninge.app.databinding.FragmentSessionResultBinding;
import com.learninge.app.databinding.ViewCelebrationBinding;
import com.learninge.app.domain.rules.FlameStage;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.Motion;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SCR-10 Kết quả phiên học.
 */
public class SessionResultFragment extends BaseFragment {
    private static final int CONFETTI_MILLIS = 1_200;
    private static final int CONFETTI_STEPS = 30;

    public SessionResultFragment() {
        super(R.layout.fragment_session_result);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentSessionResultBinding binding = FragmentSessionResultBinding.bind(view);
        padStatusBar(binding.headerResult);
        SessionSummary summary = summary(SessionResultFragmentArgs.fromBundle(requireArguments()));
        NavController nav = NavHostFragment.findNavController(this);
        boolean isReview = summary.kind() == ActivityType.REVIEW;
        binding.tvResultTitle.setText(getString(R.string.result_title, summary.displayName()));
        binding.tvResultMessage.setText(getString(isReview ? R.string.result_review : R.string.result_new,
                summary.total()));
        binding.tvResultCards.setText(String.valueOf(summary.total()));
        binding.tvResultPercent.setText(getString(R.string.result_percent, summary.percent()));
        binding.tvResultStreak.setText(String.valueOf(summary.streak()));
        renderGame(binding, summary, savedInstanceState == null);
        binding.tvResultXp.setText(getString(services().networkMonitor().isOnline() ? R.string.result_xp
                : R.string.result_xp_offline, summary.xp().xp()));
        binding.tvResultCap.setVisibility(summary.xp().isCapped() ? View.VISIBLE : View.GONE);
        binding.headerResult.setContentDescription(getString(R.string.result_description, summary.displayName(),
                summary.total(), summary.percent(), summary.streak()));
        binding.btnDone.setOnClickListener(v -> nav.navigateUp());
        binding.btnContinue.setVisibility(isReview ? View.GONE : View.VISIBLE);
        binding.btnContinue.setOnClickListener(v -> {
            nav.navigateUp();
            nav.navigate(NavMainDirections.actionGlobalFlashcard(StudySource.NEW).setDeckId(summary.deckId()));
        });
    }

    private void renderGame(FragmentSessionResultBinding binding, SessionSummary summary, boolean isFirstTime) {
        ViewCelebrationBinding celebration = ViewCelebrationBinding.bind(binding.getRoot());
        celebration.flameResult.setStreak(summary.streak());
        int stage = FlameStage.of(summary.streak());
        boolean isNewStage = stage > FlameStage.of(summary.streakBefore());
        binding.tvResultStage.setVisibility(isNewStage ? View.VISIBLE : View.GONE);
        binding.tvResultStage.setText(getString(R.string.result_new_stage, stage));
        binding.tvResultMoves.setText(getString(R.string.result_moves, summary.remembered(), summary.forgotten()));
        binding.chestsResult.showChange(summary.boxesBefore(), summary.boxesAfter());
        if (isFirstTime && Motion.isEnabled(requireContext())) {
            dropConfetti(celebration.ivConfetti);
        }
    }

    // Confetti rơi theo bước nguyên 1 pixel nghệ thuật (4 dp), một lần, không quá 1,2 s.
    private void dropConfetti(View confetti) {
        float step = getResources().getDimension(R.dimen.space_1);
        ValueAnimator fall = ValueAnimator.ofInt(-CONFETTI_STEPS, 0);
        fall.setDuration(CONFETTI_MILLIS);
        fall.setInterpolator(new LinearInterpolator());
        fall.addUpdateListener(animator -> confetti.setTranslationY((int) animator.getAnimatedValue() * step));
        fall.start();
    }

    private static SessionSummary summary(SessionResultFragmentArgs args) {
        return new SessionSummary(args.getKind(), args.getDeckId(), args.getDisplayName(), args.getRemembered(),
                args.getForgotten(), args.getStreakBefore(), args.getStreak(),
                new XpGain(args.getXp(), args.getIsXpCapped()), toList(args.getBoxesBefore()),
                toList(args.getBoxesAfter()));
    }

    private static List<Integer> toList(int[] values) {
        return Arrays.stream(values).boxed().collect(Collectors.toList());
    }
}
