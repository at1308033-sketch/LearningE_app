package com.learninge.app.ui.common;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.function.Function;

/**
 * So sánh phần tử danh sách theo khóa; nội dung so bằng {@code equals} (model là record).
 */
public final class SimpleDiff<T> extends DiffUtil.ItemCallback<T> {
    private final Function<T, Object> key;

    public SimpleDiff(Function<T, Object> key) {
        this.key = key;
    }

    @Override
    public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        return key.apply(oldItem).equals(key.apply(newItem));
    }

    // Lint không biết T luôn là record (đã có equals theo nội dung).
    @SuppressLint("DiffUtilEquals")
    @Override
    public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        return oldItem.equals(newItem);
    }
}
