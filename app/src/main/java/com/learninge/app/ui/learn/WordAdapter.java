package com.learninge.app.ui.learn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;

import com.learninge.app.data.model.WordItem;
import com.learninge.app.databinding.ItemWordBinding;
import com.learninge.app.ui.common.Badges;
import com.learninge.app.ui.common.BindingHolder;
import com.learninge.app.ui.common.SimpleDiff;

/**
 * Dòng từ dùng chung cho SCR-07 và SCR-19.
 */
public final class WordAdapter extends ListAdapter<WordItem, BindingHolder<ItemWordBinding>> {

    public WordAdapter() {
        super(new SimpleDiff<>(item -> item.word().id()));
    }

    @NonNull
    @Override
    public BindingHolder<ItemWordBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(ItemWordBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemWordBinding> holder, int position) {
        WordItem item = getItem(position);
        ItemWordBinding binding = holder.binding;
        binding.tvWordTerm.setText(item.word().term());
        binding.tvWordReading.setText(item.word().reading());
        binding.tvWordReading.setVisibility(item.word().reading() == null ? View.GONE : View.VISIBLE);
        binding.tvWordMeaning.setText(item.word().meaning());
        Badges.showStatus(binding.tvWordStatus, item.status());
    }
}
