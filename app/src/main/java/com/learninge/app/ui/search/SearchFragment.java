package com.learninge.app.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.NavMainDirections;
import com.learninge.app.R;
import com.learninge.app.data.model.WordItem;
import com.learninge.app.databinding.FragmentSearchBinding;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.learn.WordAdapter;

import java.util.List;

/**
 * SCR-18 Tra từ: tìm kiếm toàn bộ từ theo term / phiên âm / nghĩa (US-018).
 */
public class SearchFragment extends BaseFragment {

    public SearchFragment() {
        super(R.layout.fragment_search);
    }

    @Override
    protected boolean hasDarkHeader() {
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentSearchBinding binding = FragmentSearchBinding.bind(view);
        padStatusBar(binding.headerSearch);

        SearchViewModel viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        WordAdapter adapter = new WordAdapter(
                item -> NavHostFragment.findNavController(this)
                        .navigate(NavMainDirections.actionGlobalWordDetail(item.word().id())),
                item -> { /* yêu thích không cần trong màn tìm */ });
        binding.rvSearch.setAdapter(adapter);

        viewModel.results().observe(getViewLifecycleOwner(), results -> render(binding, adapter, results));

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.search(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void render(FragmentSearchBinding binding, WordAdapter adapter, List<WordItem> results) {
        boolean empty = results.isEmpty();
        binding.esSearch.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty && binding.etSearch.getText() != null && !binding.etSearch.getText().toString().isEmpty()) {
            binding.esSearch.setTitle(getString(R.string.search_no_results));
        } else {
            binding.esSearch.setTitle(getString(R.string.search_empty_title));
        }
        adapter.submitList(results);
    }
}
