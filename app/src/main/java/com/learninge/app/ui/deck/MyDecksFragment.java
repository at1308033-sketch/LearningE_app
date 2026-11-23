package com.learninge.app.ui.deck;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.learninge.app.NavMainDirections;
import com.learninge.app.R;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.databinding.FragmentMyDecksBinding;
import com.learninge.app.domain.StudyService;
import com.learninge.app.ui.common.ErrorMessages;

import java.util.List;

/**
 * SCR-26 Bộ của tôi, nằm trong tab Học (US-028).
 */
public class MyDecksFragment extends Fragment {

    public MyDecksFragment() {
        super(R.layout.fragment_my_decks);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentMyDecksBinding binding = FragmentMyDecksBinding.bind(view);
        NavController nav = NavHostFragment.findNavController(this);
        DeckAdapter adapter = new DeckAdapter(deck ->
                nav.navigate(NavMainDirections.actionGlobalDeckDetail(deck.deck().id())));
        binding.rvDecks.setAdapter(adapter);
        binding.btnCreateDeck.setOnClickListener(v ->
                nav.navigate(NavMainDirections.actionGlobalDeckEdit().setDeckId(StudyService.NO_DECK)));
        MyDecksViewModel viewModel = new ViewModelProvider(this).get(MyDecksViewModel.class);
        viewModel.decks().observe(getViewLifecycleOwner(), result -> render(binding, adapter, result));
        viewModel.load();
    }

    private void render(FragmentMyDecksBinding binding, DeckAdapter adapter, Result<List<DeckSummary>> result) {
        if (result.isOk()) {
            adapter.submitList(result.value);
        } else {
            Snackbar.make(binding.getRoot(), ErrorMessages.of(requireContext(), result.error), Snackbar.LENGTH_LONG)
                    .show();
        }
    }
}
