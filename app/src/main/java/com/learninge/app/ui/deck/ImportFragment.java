package com.learninge.app.ui.deck;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.databinding.FragmentImportBinding;
import com.learninge.app.domain.ImportExportService;
import com.learninge.app.domain.rules.DeckRules;
import com.learninge.app.domain.rules.ImportPreview;
import com.learninge.app.domain.rules.WordInput;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * SCR-28 Xem trước import (US-032).
 */
public class ImportFragment extends BaseFragment {
    private static final int MAX_ERRORS_SHOWN = 20;

    private List<DeckSummary> targets = List.of();

    public ImportFragment() {
        super(R.layout.fragment_import);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentImportBinding binding = FragmentImportBinding.bind(view);
        padStatusBar(binding.headerImport);
        binding.toolbarImport.setNavigationOnClickListener(v -> close());
        binding.btnImportCancel.setOnClickListener(v -> close());
        ImportViewModel viewModel = new ViewModelProvider(this).get(ImportViewModel.class);
        viewModel.state().observe(getViewLifecycleOwner(), state -> render(binding, viewModel, state));
        viewModel.imported().observe(getViewLifecycleOwner(), result -> onImported(binding, result));
        viewModel.load(Uri.parse(ImportFragmentArgs.fromBundle(requireArguments()).getUri()));
    }

    private void render(FragmentImportBinding binding, ImportViewModel viewModel, ImportViewModel.State state) {
        binding.piImport.setVisibility(View.GONE);
        boolean isOk = state.preview().isOk();
        binding.svImport.setVisibility(isOk ? View.VISIBLE : View.GONE);
        binding.esImport.setVisibility(isOk ? View.GONE : View.VISIBLE);
        if (!isOk) {
            binding.esImport.setTitle(errorText(state.preview()));
            binding.esImport.setAction(R.string.common_back, v -> close());
            return;
        }
        ImportPreview preview = state.preview().value;
        targets = state.targets();
        binding.toolbarImport.setSubtitle(getString(R.string.import_subtitle, preview.deckName(), preview.total()));
        binding.tvValid.setText(String.valueOf(preview.words().size()));
        binding.tvDuplicate.setText(String.valueOf(preview.duplicates()));
        binding.tvErrors.setText(String.valueOf(preview.errors().size()));
        renderErrors(binding, preview);
        renderSample(binding, preview);
        renderTargets(binding, preview);
        binding.btnImport.setEnabled(!preview.words().isEmpty());
        binding.btnImport.setText(preview.words().isEmpty() ? getString(R.string.import_nothing)
                : getString(R.string.import_submit, preview.words().size()));
        binding.btnImport.setOnClickListener(v -> {
            binding.btnImport.setEnabled(false);
            binding.piImport.setVisibility(View.VISIBLE);
            viewModel.importInto(preview, targetId(binding));
        });
    }

    private void renderErrors(FragmentImportBinding binding, ImportPreview preview) {
        binding.cardErrors.setVisibility(preview.errors().isEmpty() ? View.GONE : View.VISIBLE);
        StringBuilder lines = new StringBuilder();
        for (ImportPreview.RowError error : preview.errors().subList(0,
                Math.min(MAX_ERRORS_SHOWN, preview.errors().size()))) {
            lines.append(getString(R.string.import_error_line, error.line(), getString(reason(error.reason()))))
                    .append('\n');
        }
        if (preview.errors().size() > MAX_ERRORS_SHOWN) {
            lines.append(getString(R.string.import_error_more, preview.errors().size() - MAX_ERRORS_SHOWN));
        }
        binding.tvErrorLines.setText(lines.toString().stripTrailing());
    }

    private void renderSample(FragmentImportBinding binding, ImportPreview preview) {
        StringBuilder lines = new StringBuilder();
        for (WordInput word : preview.sample()) {
            lines.append(getString(R.string.import_sample_line, word.term().strip(), word.meaning().strip()))
                    .append('\n');
        }
        binding.cardSample.setVisibility(preview.words().isEmpty() ? View.GONE : View.VISIBLE);
        binding.tvSample.setText(lines.toString().stripTrailing());
    }

    private void renderTargets(FragmentImportBinding binding, ImportPreview preview) {
        List<String> names = new ArrayList<>();
        names.add(getString(R.string.import_new_deck, preview.deckName()));
        targets.forEach(item -> names.add(item.deck().name()));
        binding.actTarget.setAdapter(new ArrayAdapter<>(requireContext(),
                com.google.android.material.R.layout.m3_auto_complete_simple_item, names));
        if (binding.actTarget.getText().length() == 0) {
            binding.actTarget.setText(names.get(0), false);
        }
    }

    private long targetId(FragmentImportBinding binding) {
        String chosen = ((TextView) binding.actTarget).getText().toString();
        return targets.stream().filter(item -> item.deck().name().equals(chosen)).map(item -> item.deck().id())
                .findFirst().orElse(ImportExportService.NEW_DECK);
    }

    private void onImported(FragmentImportBinding binding, Result<ImportExportService.ImportResult> result) {
        binding.piImport.setVisibility(View.GONE);
        if (!result.isOk()) {
            binding.btnImport.setEnabled(true);
            Snackbar.make(binding.getRoot(), ErrorMessages.of(requireContext(), result.error), Snackbar.LENGTH_LONG)
                    .show();
            return;
        }
        // Neo vào cửa sổ để thông báo còn hiện sau khi quay lại danh sách bộ.
        Snackbar.make(requireActivity().findViewById(R.id.nav_host_main), getString(R.string.import_done,
                        result.value.imported(), result.value.skipped()), Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.bottom_nav).show();
        close();
    }

    private String errorText(Result<?> result) {
        return result.error == ErrorCode.FILE_FORMAT ? getString(R.string.import_format, result.detail)
                : ErrorMessages.of(requireContext(), result.error);
    }

    @StringRes
    private static int reason(DeckRules.Field field) {
        if (field == null) {
            return R.string.import_error_columns;
        }
        return switch (field) {
            case TERM -> R.string.import_error_term;
            case MEANING -> R.string.import_error_meaning;
            case READING -> R.string.word_error_reading;
            default -> R.string.word_error_example;
        };
    }

    private void close() {
        NavHostFragment.findNavController(this).navigateUp();
    }
}
