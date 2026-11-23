package com.learninge.app.ui.leaderboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;

import com.learninge.app.R;
import com.learninge.app.data.model.Leaderboard.Entry;
import com.learninge.app.databinding.ItemRankBinding;
import com.learninge.app.ui.common.BindingHolder;
import com.learninge.app.ui.common.SimpleDiff;

/**
 * Dòng xếp hạng. Biệt danh là duy nhất nên dùng làm khóa.
 */
final class RankAdapter extends ListAdapter<Entry, BindingHolder<ItemRankBinding>> {
    private static final int[] MEDALS = {R.drawable.px_medal_1, R.drawable.px_medal_2, R.drawable.px_medal_3};
    private static final int[] AVATARS = {R.drawable.px_avatar_head_1, R.drawable.px_avatar_head_2,
            R.drawable.px_avatar_head_3, R.drawable.px_avatar_head_4, R.drawable.px_avatar_head_5};

    RankAdapter() {
        super(new SimpleDiff<>(Entry::nickname));
    }

    /**
     * Cùng một người luôn cùng một avatar.
     */
    @DrawableRes
    static int avatar(String nickname) {
        return AVATARS[Math.floorMod(nickname.hashCode(), AVATARS.length)];
    }

    @NonNull
    @Override
    public BindingHolder<ItemRankBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BindingHolder<>(ItemRankBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<ItemRankBinding> holder, int position) {
        Entry entry = getItem(position);
        ItemRankBinding binding = holder.binding;
        Context context = binding.getRoot().getContext();
        boolean hasMedal = entry.rank() >= 1 && entry.rank() <= MEDALS.length;
        binding.ivMedal.setVisibility(hasMedal ? View.VISIBLE : View.INVISIBLE);
        binding.tvRank.setVisibility(hasMedal ? View.INVISIBLE : View.VISIBLE);
        if (hasMedal) {
            binding.ivMedal.setImageResource(MEDALS[entry.rank() - 1]);
        }
        binding.tvRank.setText(String.valueOf(entry.rank()));
        binding.ivAvatar.setImageResource(avatar(entry.nickname()));
        binding.tvNickname.setText(entry.nickname());
        binding.tvYou.setVisibility(entry.isMe() ? View.VISIBLE : View.GONE);
        binding.ivRankFlame.setImageResource(entry.streak() > 0 ? R.drawable.px_flame_badge
                : R.drawable.px_flame_badge_off);
        binding.tvRankStreak.setText(context.getResources().getQuantityString(R.plurals.days, entry.streak(),
                entry.streak()));
        binding.tvRankXp.setText(context.getString(R.string.xp, entry.xp()));
        binding.llRank.setBackgroundColor(ContextCompat.getColor(context, entry.isMe() ? R.color.primary_container
                : R.color.surface));
        binding.llRank.setContentDescription(context.getString(R.string.leaderboard_row, entry.rank(),
                entry.isMe() ? context.getString(R.string.leaderboard_you) : entry.nickname(), entry.xp(),
                entry.streak()));
    }
}
