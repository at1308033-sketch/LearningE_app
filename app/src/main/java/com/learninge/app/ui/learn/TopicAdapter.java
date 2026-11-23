package com.learninge.app.ui.learn;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;

import com.learninge.app.R;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.databinding.ItemTopicBinding;
import com.learninge.app.ui.common.Badges;
import com.learninge.app.ui.common.BindingHolder;
import com.learninge.app.ui.common.SimpleDiff;
import com.learninge.app.ui.common.TopicArt;

import java.util.function.Consumer;

/**
 * Lưới chủ đề của SCR-06.
 */
final class TopicAdapter extends ListAdapter<DeckSummary, BindingHolder<ItemTopicBinding>> {
    private final Consumer<DeckSummary> onClick;

    TopicAdapter(Consumer<DeckSummary> onClick) {
        super(new SimpleDiff<>(item -> item.deck().id()));
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public BindingHolder<ItemTopicBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(ItemTopicBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemTopicBinding> holder, int position) {
        DeckSummary item = getItem(position);
        ItemTopicBinding binding = holder.binding;
        Resources resources = binding.getRoot().getResources();
        int tile = ContextCompat.getColor(binding.getRoot().getContext(), TopicArt.tile(item.deck()));
        binding.ivTopicIcon.setImageResource(TopicArt.icon(item.deck()));
        binding.ivTopicIcon.setBackgroundTintList(ColorStateList.valueOf(tile));
        binding.vTopicBlob.setBackgroundTintList(ColorStateList.valueOf(tile));
        Badges.showLevel(binding.tvTopicLevel, item.deck().level());
        binding.tvTopicName.setText(item.deck().name());
        binding.tvTopicProgress.setText(resources.getString(R.string.topic_progress, item.mastered(), item.total()));
        binding.piTopic.setMax(Math.max(1, item.total()));
        binding.piTopic.setProgress(item.mastered());
        binding.getRoot().setContentDescription(resources.getString(R.string.topic_description, item.deck().name(),
                item.deck().level(), item.mastered(), item.total()));
        binding.getRoot().setOnClickListener(v -> onClick.accept(item));
    }
}
