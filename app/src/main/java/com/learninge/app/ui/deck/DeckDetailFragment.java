package com.learninge.app.ui.deck;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.learninge.app.NavMainDirections;
import com.learninge.app.R;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.Deck;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.databinding.FragmentDeckDetailBinding;
import com.learninge.app.domain.DeckService;
import com.learninge.app.domain.StudyService;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;
import com.learninge.app.ui.learn.WordAdapter;
import com.learninge.app.ui.study.StudySource;

import java.util.Locale;

/**
 * SCR-19 Chi tiết bộ (US-029..US-031). Bộ có sẵn và bộ ảo "Yêu thích" chỉ đọc.
 */
public class DeckDetailFragment extends BaseFragment {
    private long deckId;
    private final ActivityResultLauncher<String> saver = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/csv"), this::export);

    public DeckDetailFragment() {
        super(R.layout.fragment_deck_detail);
    }

    @Override
    protected boolean hasDarkHeader() {
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentDeckDetailBinding binding = FragmentDeckDetailBinding.bind(view);
        padStatusBar(binding.headerDeck);
        NavController nav = NavHostFragment.findNavController(this);
        deckId = DeckDetailFragmentArgs.fromBundle(requireArguments()).getDeckId();
        DeckDetailViewModel viewModel = new ViewModelProvider(this).get(DeckDetailViewModel.class);
        binding.toolbarDeck.setNavigationOnClickListener(v -> nav.navigateUp());
        binding.toolbarDeck.setOnMenuItemClickListener(item -> onMenu(item, viewModel, nav));
        WordAdapter adapter = new WordAdapter(
                item -> nav.navigate(NavMainDirections.actionGlobalWordDetail(item.word().id())),
                item -> viewModel.toggleFavorite(deckId, item.word().id()));
        binding.rvDeckWords.setAdapter(adapter);
        binding.btnDeckLearn.setOnClickListener(v ->
                nav.navigate(NavMainDirections.actionGlobalFlashcard(StudySource.NEW).setDeckId(deckId)));
        binding.fabAddWord.setOnClickListener(v -> nav.navigate(NavMainDirections.actionGlobalWordEdit(deckId)));
        viewModel.state().observe(getViewLifecycleOwner(), result -> render(binding, adapter, result));
        viewModel.deleted().observe(getViewLifecycleOwner(), result -> {
            if (result.isOk()) {
                nav.navigateUp();
            } else {
                Snackbar.make(view, ErrorMessages.of(requireContext(), result.error), Snackbar.LENGTH_LONG).show();
            }
        });
        viewModel.exported().observe(getViewLifecycleOwner(), result -> Snackbar.make(view, result.isOk()
                ? getResources().getQuantityString(R.plurals.deck_exported, result.value, result.value)
                : getString(R.string.error_export), Snackbar.LENGTH_LONG).show());
        viewModel.load(deckId);
    }

    private void export(Uri target) {
        // Người dùng hủy chọn nơi lưu thì target là null.
        if (target != null) {
            new ViewModelProvider(this).get(DeckDetailViewModel.class).export(deckId, target);
        }
    }

    private boolean onMenu(MenuItem item, DeckDetailViewModel viewModel, NavController nav) {
        if (item.getItemId() == R.id.action_edit_deck) {
            nav.navigate(NavMainDirections.actionGlobalDeckEdit().setDeckId(deckId));
        } else {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.deck_delete_title)
                    .setMessage(R.string.deck_delete_message)
                    .setNegativeButton(R.string.common_cancel, null)
                    .setPositiveButton(R.string.common_delete, (dialog, which) -> viewModel.delete(deckId))
                    .show();
        }
        return true;
    }

    private void render(FragmentDeckDetailBinding binding, WordAdapter adapter,
                        Result<DeckDetailViewModel.State> result) {
        if (!result.isOk()) {
            binding.esDeck.setVisibility(View.VISIBLE);
            binding.esDeck.setTitle(ErrorMessages.of(requireContext(), result.error));
            return;
        }
        DeckSummary summary = result.value.deck();
        Deck deck = summary.deck();
        boolean isFavorites = deck.id() == DeckService.FAVORITES;
        boolean isEditable = !deck.isBuiltin();
        binding.toolbarDeck.setTitle(deck.name());
        binding.toolbarDeck.getMenu().findItem(R.id.action_edit_deck).setVisible(isEditable);
        binding.toolbarDeck.getMenu().findItem(R.id.action_delete_deck).setVisible(isEditable && !deck.isDefault());
        binding.tvDeckInfo.setText(isFavorites
                ? getResources().getQuantityString(R.plurals.words, summary.total(), summary.total())
                : getString(R.string.deck_info, deck.languageCode().toUpperCase(Locale.ROOT), summary.total(),
                summary.due()));
        binding.tvDeckReadOnly.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        binding.fabAddWord.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        binding.btnDeckExport.setEnabled(summary.total() > 0);
        binding.btnDeckExport.setOnClickListener(v ->
                saver.launch(services().importExportService().exportName(deck.name())));
        binding.btnDeckLearn.setEnabled(summary.newWords() > 0);
        binding.esDeck.setVisibility(summary.total() == 0 ? View.VISIBLE : View.GONE);
        binding.esDeck.setTitle(isFavorites ? R.string.favorites_empty : R.string.word_list_empty);
        adapter.submitList(result.value.words());
    }
}
