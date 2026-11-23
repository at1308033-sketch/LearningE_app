package com.learninge.app.ui.common;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

/**
 * ViewHolder chỉ giữ binding, dùng cho mọi danh sách.
 */
public final class BindingHolder<B extends ViewBinding> extends RecyclerView.ViewHolder {
    public final B binding;

    public BindingHolder(B binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
