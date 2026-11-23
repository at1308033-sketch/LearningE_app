package com.learninge.app.ui.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;

/**
 * Tôn trọng "Xóa hiệu ứng" của hệ thống (UI-UX-SPECIFICATION §Motion).
 */
public final class Motion {

    private Motion() {
    }

    public static boolean isEnabled(Context context) {
        return Settings.Global.getFloat(context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1) > 0;
    }

    public static void onEnd(Animator animator, Runnable action) {
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                action.run();
            }
        });
    }
}
