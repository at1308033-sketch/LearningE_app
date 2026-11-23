package com.learninge.app.ui.learn;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.NavMainDirections;
import com.learninge.app.R;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.databinding.FragmentWordListBinding;
import com.learninge.app.ui.common.Badges;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;
import com.learninge.app.ui.common.TopicArt;

/**
 * SCR-07 Danh sách từ của chủ đề (US-008).
 */
public class WordListFragment extends BaseFragment {

    public WordListFragment() {
        super(R.layout.fragment_word_list);
    }

    @Override
    protected boolean hasDarkHeader() {
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentWordListBinding binding = FragmentWordListBinding.bind(view);
        padStatusBar(binding.headerWordList);
        binding.toolbarWordList.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        WordListViewModel viewModel = new ViewModelProvider(this).get(WordListViewModel.class);
        long deckId = WordListFragmentArgs.fromBundle(requireArguments()).getDeckId();
        WordAdapter adapter = new WordAdapter(item -> NavHostFragment.findNavController(this)
                .navigate(NavMainDirections.actionGlobalWordDetail(item.word().id())),
                item -> viewModel.toggleFavorite(deckId, item.word().id()));
        binding.rvWords.setAdapter(adapter);
        viewModel.state().observe(getViewLifecycleOwner(), state -> render(binding, adapter, state));
        viewModel.load(deckId);
    }

    private void render(FragmentWordListBinding binding, WordAdapter adapter, Result<WordListViewModel.State> result) {
        binding.esWords.setVisibility(result.isOk() && !result.value.words().isEmpty() ? View.GONE : View.VISIBLE);
        if (!result.isOk()) {
            binding.esWords.setTitle(ErrorMessages.of(requireContext(), result.error));
            return;
        }
        adapter.submitList(result.value.words());
        renderHeader(binding, result.value.deck());
    }

    private void renderHeader(FragmentWordListBinding binding, DeckSummary deck) {
        binding.toolbarWordList.setTitle(deck.deck().name());
        binding.ivDeckIcon.setImageResource(TopicArt.icon(deck.deck()));
        binding.ivDeckIcon.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), TopicArt.tile(deck.deck()))));
        Badges.showLevel(binding.tvDeckLevel, deck.deck().level());
        binding.tvDeckCounts.setText(getString(R.string.word_list_counts, deck.total(), deck.learning()));
        binding.tvDeckMastered.setText(getString(R.string.word_list_mastered, deck.mastered(), deck.total()));
        binding.piDeck.setMax(Math.max(1, deck.total()));
        binding.piDeck.setProgress(deck.mastered());
    }
}
