package com.learninge.app.ui.common;

import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.learninge.app.LearningEApp;
import com.learninge.app.core.ServiceLocator;

/**
 * Phần chung của mọi màn hình: lấy Service, màu icon thanh trạng thái, chừa chỗ cho thanh trạng thái.
 */
public abstract class BaseFragment extends Fragment {

    protected BaseFragment(@LayoutRes int layout) {
        super(layout);
    }

    /**
     * true nếu phần đầu màn hình có nền đậm nên icon thanh trạng thái phải màu trắng.
     */
    protected boolean hasDarkHeader() {
        return false;
    }

    protected final ServiceLocator services() {
        return LearningEApp.services(requireContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requireActivity() instanceof StatusBarHost) {
            ((StatusBarHost) requireActivity()).setDarkHeader(hasDarkHeader());
        } else {
            WindowCompat.getInsetsController(requireActivity().getWindow(), requireView())
                    .setAppearanceLightStatusBars(!hasDarkHeader());
        }
    }

    /**
     * Cộng chiều cao thanh trạng thái vào lề trên của phần đầu màn hình (edge-to-edge).
     */
    protected static void padStatusBar(View header) {
        int top = header.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(header, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(view.getPaddingLeft(), top + bars.top, view.getPaddingRight(), view.getPaddingBottom());
            return insets;
        });
        ViewCompat.requestApplyInsets(header);
    }
}
