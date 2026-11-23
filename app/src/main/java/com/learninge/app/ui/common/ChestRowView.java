package com.learninge.app.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.learninge.app.R;
import com.learninge.app.databinding.ViewChestBinding;
import com.learninge.app.domain.rules.LeitnerScheduler;

import java.util.List;

/**
 * Năm rương Leitner (BR-071): số thẻ mỗi hộp, kèm khoảng ôn hoặc chênh lệch sau phiên. Không chỉ dựa vào màu.
 */
public final class ChestRowView extends LinearLayout {
    private static final int[] CHESTS = {R.drawable.px_chest_1, R.drawable.px_chest_2, R.drawable.px_chest_3,
            R.drawable.px_chest_4, R.drawable.px_chest_5};

    private final ViewChestBinding[] columns = new ViewChestBinding[CHESTS.length];

    public ChestRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setBaselineAligned(false);
        for (int i = 0; i < columns.length; i++) {
            columns[i] = ViewChestBinding.inflate(LayoutInflater.from(context), this, false);
            columns[i].ivChest.setImageResource(CHESTS[i]);
            columns[i].tvChestBox.setText(context.getString(R.string.chest_box, i + 1));
            addView(columns[i].getRoot());
        }
    }

    /**
     * SCR-21: số thẻ và khoảng ôn.
     */
    public void showCounts(List<Integer> counts) {
        for (int i = 0; i < columns.length; i++) {
            int days = LeitnerScheduler.intervalDays(i + 1);
            columns[i].tvChestCount.setText(String.valueOf(counts.get(i)));
            columns[i].tvChestDelta.setVisibility(View.GONE);
            columns[i].tvChestInterval.setText(getResources().getQuantityString(R.plurals.days, days, days));
            columns[i].getRoot().setContentDescription(getResources().getString(R.string.chest_description, i + 1,
                    counts.get(i), days));
        }
    }

    /**
     * SCR-10: số thẻ sau phiên và chênh lệch so với trước phiên.
     */
    public void showChange(List<Integer> before, List<Integer> after) {
        for (int i = 0; i < columns.length; i++) {
            int delta = after.get(i) - before.get(i);
            String text = delta > 0 ? "+" + delta : String.valueOf(delta).replace('-', '−');
            columns[i].tvChestCount.setText(String.valueOf(after.get(i)));
            columns[i].tvChestInterval.setVisibility(View.GONE);
            columns[i].tvChestDelta.setVisibility(View.VISIBLE);
            columns[i].tvChestDelta.setText(text);
            columns[i].tvChestDelta.setBackgroundTintList(ContextCompat.getColorStateList(getContext(),
                    delta > 0 ? R.color.success_container : R.color.surface_variant));
            columns[i].tvChestDelta.setTextColor(ContextCompat.getColor(getContext(),
                    delta > 0 ? R.color.success : R.color.on_surface_variant));
            columns[i].getRoot().setContentDescription(getResources().getString(R.string.chest_change, i + 1,
                    after.get(i), text));
        }
    }
}
