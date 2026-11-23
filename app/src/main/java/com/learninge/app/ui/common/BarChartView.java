package com.learninge.app.ui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.learninge.app.R;

import java.util.List;

/**
 * Biểu đồ cột tự vẽ (DEC-005): số trên cột, nhãn dưới cột, cột cuối (hôm nay) màu nhấn.
 */
public final class BarChartView extends View {

    /**
     * Một cột.
     */
    public record Bar(String label, int value) {
    }

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final int normalColor;
    private final int todayColor;
    private final float gap;
    private final float radius;
    private final float minBar;
    private List<Bar> bars = List.of();

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        normalColor = ContextCompat.getColor(context, R.color.chart_bar);
        todayColor = ContextCompat.getColor(context, R.color.primary);
        gap = getResources().getDimension(R.dimen.space_2);
        radius = getResources().getDimension(R.dimen.radius_s);
        minBar = getResources().getDimension(R.dimen.space_1);
        setUp(labelPaint, R.color.on_surface_variant);
        setUp(valuePaint, R.color.on_surface);
        valuePaint.setFakeBoldText(true);
    }

    public void setBars(List<Bar> newBars) {
        bars = newBars;
        StringBuilder description = new StringBuilder();
        for (Bar bar : newBars) {
            description.append(bar.label()).append(": ").append(bar.value()).append(", ");
        }
        setContentDescription(description);
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (bars.isEmpty()) {
            return;
        }
        int max = Math.max(1, bars.stream().mapToInt(Bar::value).max().orElse(1));
        float textHeight = labelPaint.getTextSize();
        float bottom = getHeight() - textHeight - gap;
        float room = bottom - textHeight - gap;
        float slot = (float) getWidth() / bars.size();
        for (int i = 0; i < bars.size(); i++) {
            Bar bar = bars.get(i);
            float height = Math.max(minBar, room * bar.value() / max);
            float left = slot * i + gap / 2;
            rect.set(left, bottom - height, left + slot - gap, bottom);
            barPaint.setColor(i == bars.size() - 1 ? todayColor : normalColor);
            canvas.drawRoundRect(rect, radius, radius, barPaint);
            canvas.drawText(String.valueOf(bar.value()), rect.centerX(), rect.top - gap / 2, valuePaint);
            canvas.drawText(bar.label(), rect.centerX(), getHeight() - gap / 2, labelPaint);
        }
    }

    private void setUp(Paint paint, int color) {
        paint.setColor(ContextCompat.getColor(getContext(), color));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(getResources().getDimension(R.dimen.chart_text));
        if (!isInEditMode()) {
            paint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.lexend));
        }
    }
}
