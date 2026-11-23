package com.learninge.app.ui.common;

import android.content.res.ColorStateList;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.learninge.app.R;
import com.learninge.app.domain.rules.LeitnerScheduler.Status;

/**
 * Nhãn nhỏ dùng chung: cấp độ A1/A2/B1 và trạng thái từ. Luôn có chữ, không chỉ dựa vào màu.
 */
public final class Badges {

    private Badges() {
    }

    public static void showLevel(TextView view, String level) {
        view.setVisibility(level == null ? TextView.GONE : TextView.VISIBLE);
        view.setText(level);
        paint(view, R.drawable.bg_pill, "A1".equals(level) ? R.color.level_a1
                : "A2".equals(level) ? R.color.level_a2 : R.color.level_b1, R.color.on_primary);
    }

    public static void showStatus(TextView view, Status status) {
        switch (status) {
            case MASTERED:
                view.setText(R.string.word_status_mastered);
                paint(view, R.drawable.bg_pill, R.color.success_container, R.color.success);
                break;
            case LEARNING:
                view.setText(R.string.word_status_learning);
                paint(view, R.drawable.bg_pill, R.color.primary_container, R.color.on_primary_container);
                break;
            default:
                view.setText(R.string.word_status_new);
                view.setBackgroundResource(R.drawable.bg_pill_outlined);
                view.setBackgroundTintList(null);
                view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.on_surface));
                break;
        }
    }

    private static void paint(TextView view, int shape, @ColorRes int background, @ColorRes int text) {
        view.setBackgroundResource(shape);
        view.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(view.getContext(), background)));
        view.setTextColor(ContextCompat.getColor(view.getContext(), text));
    }
}
