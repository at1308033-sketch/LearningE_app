package com.learninge.app.ui.common;

import androidx.core.view.ViewCompat;

import com.google.android.material.button.MaterialButton;
import com.learninge.app.R;

/**
 * Nút trái tim: icon đặc khi đã thích, trạng thái đọc được bằng TalkBack.
 */
public final class HeartButton {

    private HeartButton() {
    }

    public static void show(MaterialButton button, boolean isFavorite) {
        button.setIconResource(isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
        button.setContentDescription(button.getResources().getString(R.string.favorite));
        ViewCompat.setStateDescription(button, button.getResources().getString(isFavorite
                ? R.string.favorite_on : R.string.favorite_off));
    }
}
