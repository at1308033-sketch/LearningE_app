package com.learninge.app.ui.common;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.learninge.app.R;
import com.learninge.app.data.model.Language;

/**
 * Nút loa: đọc chữ hoặc báo thiếu giọng kèm nút mở cài đặt giọng đọc.
 */
public final class Speaker {
    private static final String TTS_SETTINGS = "com.android.settings.TTS_SETTINGS";

    private Speaker() {
    }

    public static void bind(View button, TtsHelper tts, String text, Language language) {
        button.setOnClickListener(v -> tts.speak(text, language.ttsLocale(), () -> showUnavailable(button, language)));
    }

    private static void showUnavailable(View anchor, Language language) {
        Snackbar.make(anchor, anchor.getResources().getString(R.string.tts_unavailable, language.name()),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.tts_settings, v -> {
                    try {
                        v.getContext().startActivity(new Intent(TTS_SETTINGS));
                    } catch (ActivityNotFoundException noSettings) {
                        Snackbar.make(anchor, R.string.tts_no_settings, Snackbar.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
