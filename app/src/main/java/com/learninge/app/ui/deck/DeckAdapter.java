package com.learninge.app.ui.deck;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;

import com.learninge.app.R;
import com.learninge.app.data.model.Deck;
import com.learninge.app.data.model.DeckSummary;
import com.learninge.app.databinding.ItemDeckBinding;
import com.learninge.app.domain.DeckService;
import com.learninge.app.ui.common.BindingHolder;
import com.learninge.app.ui.common.SimpleDiff;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Thẻ bộ từ ở SCR-26; màu huy hiệu theo loại bộ.
 */
final class DeckAdapter extends ListAdapter<DeckSummary, BindingHolder<ItemDeckBinding>> {
    private final Consumer<DeckSummary> onClick;

    DeckAdapter(Consumer<DeckSummary> onClick) {
        super(new SimpleDiff<>(item -> item.deck().id()));
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public BindingHolder<ItemDeckBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(ItemDeckBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemDeckBinding> holder, int position) {
        DeckSummary item = getItem(position);
        ItemDeckBinding binding = holder.binding;
        Resources resources = binding.getRoot().getResources();
        Deck deck = item.deck();
        boolean isFavorites = deck.id() == DeckService.FAVORITES;
        binding.ivDeckBadge.setImageResource(icon(deck));
        binding.ivDeckBadge.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(binding.getRoot().getContext(), color(deck))));
        binding.tvDeckName.setText(deck.name());
        binding.ivDeckLock.setVisibility(deck.isBuiltin() ? View.VISIBLE : View.GONE);
        binding.tvDeckLanguage.setText(deck.languageCode().toUpperCase(Locale.ROOT));
        binding.tvDeckLanguage.setVisibility(isFavorites ? View.GONE : View.VISIBLE);
        binding.tvDeckWords.setText(resources.getQuantityString(R.plurals.words, item.total(), item.total()));
        binding.tvDeckDue.setText(resources.getString(R.string.deck_due, item.due()));
        binding.tvDeckDue.setVisibility(item.due() > 0 && !deck.isBuiltin() ? View.VISIBLE : View.GONE);
        binding.getRoot().setOnClickListener(v -> onClick.accept(item));
    }

    private static int icon(Deck deck) {
        if (deck.id() == DeckService.FAVORITES) {
            return R.drawable.ic_heart;
        }
        return deck.isDefault() ? R.drawable.ic_notebook : deck.examTag() != null ? R.drawable.ic_target
                : R.drawable.ic_book_open;
    }

    private static int color(Deck deck) {
        if (deck.isDefault()) {
            return R.color.feat_streak;
        }
        boolean isMine = !deck.isBuiltin() || deck.id() == DeckService.FAVORITES;
        return isMine ? R.color.feat_deck : deck.examTag() != null ? R.color.feat_exam : R.color.feat_learn;
    }
}
