package com.learninge.app.ui.learn;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.learninge.app.NavMainDirections;
import com.learninge.app.R;
import com.learninge.app.databinding.FragmentLearnBinding;
import com.learninge.app.ui.common.BaseFragment;
import com.learninge.app.ui.common.ErrorMessages;

import java.util.Map;

/**
 * SCR-06 Học: lưới chủ đề và lọc cấp độ (US-007).
 */
public class LearnFragment extends BaseFragment {
    private static final Map<Integer, String> LEVEL_OF_CHIP = Map.of(R.id.chip_a1, "A1", R.id.chip_a2, "A2",
            R.id.chip_b1, "B1");

    public LearnFragment() {
        super(R.layout.fragment_learn);
    }

    @Override
    protected boolean hasDarkHeader() {
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentLearnBinding binding = FragmentLearnBinding.bind(view);
        padStatusBar(binding.headerLearn);
        TopicAdapter adapter = new TopicAdapter(item -> NavHostFragment.findNavController(this)
                .navigate(NavMainDirections.actionGlobalWordList(item.deck().id())));
        binding.rvTopics.setAdapter(adapter);
        LearnViewModel viewModel = new ViewModelProvider(this).get(LearnViewModel.class);
        viewModel.state().observe(getViewLifecycleOwner(), state -> render(binding, adapter, viewModel, state));
        binding.tgLearn.addOnButtonCheckedListener((group, id, isChecked) -> {
            if (isChecked) {
                viewModel.showDecks(id == R.id.btn_tab_decks);
            }
        });
        viewModel.isDecksShown().observe(getViewLifecycleOwner(), isDecks -> showTab(binding, isDecks));
        viewModel.load();
    }

    private static void showTab(FragmentLearnBinding binding, boolean isDecks) {
        binding.tgLearn.check(isDecks ? R.id.btn_tab_decks : R.id.btn_tab_topics);
        binding.fcvDecks.setVisibility(isDecks ? View.VISIBLE : View.GONE);
        binding.flTopics.setVisibility(isDecks ? View.GONE : View.VISIBLE);
        binding.hsLevels.setVisibility(isDecks ? View.GONE : View.VISIBLE);
    }

    private void render(FragmentLearnBinding binding, TopicAdapter adapter, LearnViewModel viewModel,
                        LearnViewModel.State state) {
        // Đặt chip theo trạng thái trước khi gắn sự kiện, để không tự kích hoạt lọc lại.
        binding.cgLevel.setOnCheckedStateChangeListener(null);
        binding.cgLevel.check(chipOf(state.level()));
        binding.cgLevel.setOnCheckedStateChangeListener((group, ids) ->
                viewModel.chooseLevel(ids.isEmpty() ? null : LEVEL_OF_CHIP.get(ids.get(0))));
        boolean isEmpty = !state.topics().isOk() || state.topics().value.isEmpty();
        binding.esLearn.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (state.topics().isOk()) {
            binding.esLearn.setTitle(R.string.learn_empty_title);
            binding.esLearn.setMessage(R.string.learn_empty_message);
            adapter.submitList(state.topics().value);
        } else {
            binding.esLearn.setTitle(ErrorMessages.of(requireContext(), state.topics().error));
            binding.esLearn.setAction(R.string.common_retry, v -> viewModel.load());
        }
    }

    private static int chipOf(String level) {
        return LEVEL_OF_CHIP.entrySet().stream()
                .filter(entry -> entry.getValue().equals(level))
                .map(Map.Entry::getKey)
                .findFirst().orElse(R.id.chip_all);
    }
}
