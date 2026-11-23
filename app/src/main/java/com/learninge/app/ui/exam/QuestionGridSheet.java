package com.learninge.app.ui.exam;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.learninge.app.R;
import com.learninge.app.data.model.ExamQuestion;
import com.learninge.app.data.model.ExamSession;
import com.learninge.app.databinding.ItemQuestionCellBinding;
import com.learninge.app.databinding.SheetQuestionGridBinding;

import java.util.function.IntConsumer;

/**
 * Bảng câu hỏi của SCR-31: 4 trạng thái ô, có chú thích chữ.
 */
final class QuestionGridSheet {

    private QuestionGridSheet() {
    }

    static void show(Context context, ExamSession session, int currentIndex, IntConsumer onPick, Runnable onSubmit) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        SheetQuestionGridBinding binding = SheetQuestionGridBinding.inflate(LayoutInflater.from(context));
        int total = session.questions().size();
        binding.tvLegend.setText(context.getString(R.string.exam_legend, session.answered(),
                total - session.answered(), session.flagged()));
        for (int i = 0; i < total; i++) {
            ItemQuestionCellBinding cell = ItemQuestionCellBinding.inflate(LayoutInflater.from(context),
                    binding.glQuestions, true);
            int index = i;
            paint(context, cell.tvCell, session.questions().get(i), i == currentIndex);
            cell.tvCell.setOnClickListener(v -> {
                dialog.dismiss();
                onPick.accept(index);
            });
        }
        binding.btnSheetSubmit.setOnClickListener(v -> {
            dialog.dismiss();
            onSubmit.run();
        });
        dialog.setContentView(binding.getRoot());
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.show();
    }

    private static void paint(Context context, TextView cell, ExamQuestion question, boolean isCurrent) {
        boolean isAnswered = question.selectedIndex() != null;
        cell.setText(String.valueOf(question.number()));
        int background = isCurrent ? R.color.feat_exam : isAnswered ? R.color.feat_exam_container : R.color.surface;
        cell.setBackgroundTintList(null);
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(context.getResources().getDimension(R.dimen.radius_s));
        shape.setColor(ColorStateList.valueOf(ContextCompat.getColor(context, background)));
        shape.setStroke(context.getResources().getDimensionPixelSize(question.isFlagged() ? R.dimen.stroke_flag
                : R.dimen.stroke_card), ContextCompat.getColor(context, question.isFlagged()
                ? R.color.feat_streak : R.color.outline_strong));
        cell.setBackground(shape);
        cell.setTextColor(ContextCompat.getColor(context, isCurrent ? R.color.on_feat_exam : R.color.on_surface));
        cell.setContentDescription(context.getString(R.string.exam_cell, question.number(),
                context.getString(isAnswered ? R.string.exam_cell_done : R.string.exam_cell_empty),
                question.isFlagged() ? context.getString(R.string.exam_cell_flagged) : ""));
    }
}
