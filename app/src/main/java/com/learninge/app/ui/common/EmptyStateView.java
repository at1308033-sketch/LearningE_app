package com.learninge.app.ui.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.StringRes;

import com.learninge.app.R;
import com.learninge.app.databinding.ViewEmptyStateBinding;

/**
 * Trạng thái rỗng/lỗi dùng chung: minh họa, tiêu đề, mô tả và một nút (UI-UX-SPECIFICATION §Component list).
 */
public final class EmptyStateView extends LinearLayout {
    private final ViewEmptyStateBinding binding;

    public EmptyStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        binding = ViewEmptyStateBinding.inflate(LayoutInflater.from(context), this);
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.EmptyStateView);
        try {
            binding.ivEmptyArt.setImageDrawable(values.getDrawable(R.styleable.EmptyStateView_emptyArt));
            binding.ivEmptyArt.setVisibility(binding.ivEmptyArt.getDrawable() == null ? GONE : VISIBLE);
            setText(binding.tvEmptyTitle, values.getText(R.styleable.EmptyStateView_emptyTitle));
            setText(binding.tvEmptyMessage, values.getText(R.styleable.EmptyStateView_emptyMessage));
        } finally {
            values.recycle();
        }
    }

    public void setTitle(@StringRes int title) {
        setText(binding.tvEmptyTitle, getContext().getText(title));
    }

    public void setTitle(CharSequence title) {
        setText(binding.tvEmptyTitle, title);
    }

    public void setMessage(@StringRes int message) {
        setText(binding.tvEmptyMessage, getContext().getText(message));
    }

    public void setAction(@StringRes int label, View.OnClickListener onClick) {
        binding.btnEmptyAction.setText(label);
        binding.btnEmptyAction.setVisibility(VISIBLE);
        binding.btnEmptyAction.setOnClickListener(onClick);
    }

    private static void setText(android.widget.TextView view, CharSequence text) {
        view.setText(text);
        view.setVisibility(text == null ? GONE : VISIBLE);
    }
}
