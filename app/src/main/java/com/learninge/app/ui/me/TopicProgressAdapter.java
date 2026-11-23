package com.learninge.app.ui.me;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;

import com.learninge.app.R;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.databinding.ItemTopicProgressBinding;
import com.learninge.app.ui.common.BindingHolder;
import com.learninge.app.ui.common.SimpleDiff;
import com.learninge.app.ui.common.TopicArt;

/**
 * Tiến độ từng chủ đề.
 */
final class TopicProgressAdapter extends ListAdapter<DeckSummary, BindingHolder<ItemTopicProgressBinding>> {

    TopicProgressAdapter() {
        super(new SimpleDiff<>(item -> item.deck().id()));
    }

    @NonNull
    @Override
    public BindingHolder<ItemTopicProgressBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(ItemTopicProgressBinding.inflate(LayoutInflater.from(parent.getContext()), parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemTopicProgressBinding> holder, int position) {
        DeckSummary item = getItem(position);
        ItemTopicProgressBinding binding = holder.binding;
        binding.ivTopic.setImageResource(TopicArt.icon(item.deck()));
        binding.ivTopic.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(binding.getRoot().getContext(), TopicArt.tile(item.deck()))));
        binding.tvTopic.setText(item.deck().name());
        binding.tvTopicCount.setText(binding.getRoot().getResources()
                .getString(R.string.home_deck_count, item.mastered(), item.total()));
        binding.piTopic.setMax(Math.max(1, item.total()));
        binding.piTopic.setProgress(item.mastered());
    }
}
