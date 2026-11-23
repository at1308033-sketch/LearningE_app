package com.learninge.app.ui.exam;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.learninge.app.NavMainDirections;
import com.learninge.app.R;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.data.model.ExamTest;
import com.learninge.app.databinding.FragmentExamHubBinding;
import com.learninge.app.databinding.ItemExamGroupBinding;
import com.learninge.app.databinding.ItemExamRowBinding;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;

import java.util.List;

/**
 * SCR-30 Luyện thi (US-035).
 */
public class ExamHubFragment extends BaseFragment {
    private static final int SECONDS_PER_MINUTE = 60;

    public ExamHubFragment() {
        super(R.layout.fragment_exam_hub);
    }

    @Override
    protected boolean hasDarkHeader() {
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentExamHubBinding binding = FragmentExamHubBinding.bind(view);
        padStatusBar(binding.headerExamHub);
        NavController nav = NavHostFragment.findNavController(this);
        binding.toolbarExamHub.setNavigationOnClickListener(v -> nav.navigateUp());
        binding.rowHistory.setOnClickListener(v -> nav.navigate(NavMainDirections.actionGlobalExamHistory()));
        binding.rowResources.setOnClickListener(v -> nav.navigate(NavMainDirections.actionGlobalExamResources()));
        ExamHubViewModel viewModel = new ViewModelProvider(this).get(ExamHubViewModel.class);
        viewModel.groups().observe(getViewLifecycleOwner(), result -> render(binding, nav, result));
        viewModel.load();
    }

    private void render(FragmentExamHubBinding binding, NavController nav,
                        Result<List<ExamHubViewModel.Group>> result) {
        binding.llGroups.removeAllViews();
        if (!result.isOk()) {
            Snackbar.make(binding.getRoot(), ErrorMessages.of(requireContext(), result.error), Snackbar.LENGTH_LONG)
                    .show();
            return;
        }
        result.value.forEach(group -> addGroup(binding.llGroups, nav, group));
    }

    private void addGroup(ViewGroup parent, NavController nav, ExamHubViewModel.Group group) {
        ItemExamGroupBinding item = ItemExamGroupBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, true);
        item.tvExamBadge.setText(label(group.tag()));
        item.tvExamBadge.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), color(group.tag()))));
        item.tvExamName.setText(label(group.tag()));
        item.tvExamNote.setText(note(group.tag()));
        for (DeckSummary deck : group.decks()) {
            ItemExamRowBinding row = ItemExamRowBinding.inflate(LayoutInflater.from(item.llRows.getContext()),
                            item.llRows, true);
            row.ivRow.setImageResource(R.drawable.ic_book_open);
            row.tvRowTitle.setText(getString(R.string.exam_deck, deck.deck().name(), deck.total()));
            row.tvRowNote.setText(R.string.deck_read_only);
            row.btnRow.setVisibility(View.GONE);
            row.getRoot().setOnClickListener(v ->
                    nav.navigate(NavMainDirections.actionGlobalDeckDetail(deck.deck().id())));
        }
        for (ExamTest test : group.tests()) {
            ItemExamRowBinding row = ItemExamRowBinding.inflate(LayoutInflater.from(item.llRows.getContext()),
                            item.llRows, true);
            row.ivRow.setImageResource(R.drawable.ic_clipboard_check);
            row.tvRowTitle.setText(test.title());
            row.tvRowNote.setText(test.bestScore() == null
                    ? getString(R.string.exam_info, test.questionCount(), test.durationSec() / SECONDS_PER_MINUTE)
                    : getString(R.string.exam_info_best, test.questionCount(),
                    test.durationSec() / SECONDS_PER_MINUTE, test.bestScore()));
            row.ivRowChevron.setVisibility(View.GONE);
            row.btnRow.setText(test.isInProgress() ? R.string.exam_resume : R.string.exam_start);
            row.btnRow.setOnClickListener(v -> nav.navigate(NavMainDirections.actionGlobalExam(test.code())));
        }
        item.tvNoContent.setVisibility(group.decks().isEmpty() && group.tests().isEmpty() ? View.VISIBLE : View.GONE);
    }

    private static String label(String tag) {
        return "APTIS".equals(tag) ? "Aptis" : tag;
    }

    @ColorRes
    private static int color(String tag) {
        return "TOEIC".equals(tag) ? R.color.feat_exam : "IELTS".equals(tag) ? R.color.feat_quiz : R.color.feat_learn;
    }

    @StringRes
    private static int note(String tag) {
        return "TOEIC".equals(tag) ? R.string.exam_toeic : "IELTS".equals(tag) ? R.string.exam_ielts
                : R.string.exam_aptis;
    }
}
