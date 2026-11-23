package com.learninge.app.ui.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.learninge.app.R;
import com.learninge.app.databinding.ViewFeedbackBannerBinding;

/**
 * Thẻ thông báo trong trang: đúng, sai/lỗi, thông tin, cảnh báo (UI-UX-SPECIFICATION §Feedback banner).
 */
public final class FeedbackBannerView extends MaterialCardView {

    public enum Kind {
        SUCCESS(R.color.success_container, R.color.success, R.drawable.ic_check),
        ERROR(R.color.error_container, R.color.error, R.drawable.ic_info),
        INFO(R.color.primary_container, R.color.on_primary_container, R.drawable.ic_info),
        WARNING(R.color.feat_streak_container, R.color.feat_streak, R.drawable.ic_info);

        @ColorRes
        private final int background;
        @ColorRes
        private final int foreground;
        @DrawableRes
        private final int icon;

        Kind(int background, int foreground, int icon) {
            this.background = background;
            this.foreground = foreground;
            this.icon = icon;
        }
    }

    private final ViewFeedbackBannerBinding binding;

    public FeedbackBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        binding = ViewFeedbackBannerBinding.inflate(LayoutInflater.from(context), this);
        setCardElevation(0);
        setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);
    }

    public void show(Kind kind, CharSequence message) {
        int foreground = ContextCompat.getColor(getContext(), kind.foreground);
        setCardBackgroundColor(ContextCompat.getColor(getContext(), kind.background));
        binding.ivBanner.setImageResource(kind.icon);
        binding.ivBanner.setImageTintList(ColorStateList.valueOf(foreground));
        binding.tvBanner.setTextColor(foreground);
        binding.tvBanner.setText(message);
        setVisibility(VISIBLE);
    }

    public void hide() {
        setVisibility(GONE);
    }
}
