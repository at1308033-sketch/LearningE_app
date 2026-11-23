package com.learninge.app.ui.common;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import com.learninge.app.R;
import com.learninge.app.domain.rules.FlameStage;

/**
 * Ngọn lửa pixel theo chuỗi ngày (BR-070): 2 khung, chỉ chạy khi đang hiển thị và hệ thống không tắt hiệu ứng.
 */
public final class FlameView extends AppCompatImageView {
    private static final int[] ANIMATIONS = {R.drawable.px_flame_anim_0, R.drawable.px_flame_anim_1,
            R.drawable.px_flame_anim_2, R.drawable.px_flame_anim_3, R.drawable.px_flame_anim_4};

    public FlameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.FIT_XY);
    }

    public void setStreak(int streak) {
        int stage = FlameStage.of(streak);
        setImageDrawable(AppCompatResources.getDrawable(getContext(), animation(stage)));
        setContentDescription(describe(streak, stage));
        updateAnimation();
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        updateAnimation();
    }

    @DrawableRes
    private static int animation(int stage) {
        return ANIMATIONS[stage];
    }

    private void updateAnimation() {
        Drawable drawable = getDrawable();
        if (!(drawable instanceof AnimationDrawable)) {
            return;
        }
        AnimationDrawable flame = (AnimationDrawable) drawable;
        if (isShown() && Motion.isEnabled(getContext())) {
            flame.start();
        } else {
            flame.stop();
            flame.selectDrawable(0);
        }
    }

    private String describe(int streak, int stage) {
        if (stage == 0) {
            return getResources().getString(R.string.progress_no_streak);
        }
        return stage == FlameStage.MAX_STAGE ? getResources().getString(R.string.flame_max, streak)
                : getResources().getString(R.string.flame_description, streak, stage, FlameStage.daysToNext(streak),
                stage + 1);
    }
}
