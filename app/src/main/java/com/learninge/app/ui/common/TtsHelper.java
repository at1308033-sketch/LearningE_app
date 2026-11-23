package com.learninge.app.ui.common;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.Locale;

/**
 * Đọc từ bằng giọng của máy (US-010). Gắn vào vòng đời màn hình để tự tắt khi rời đi.
 */
public final class TtsHelper implements DefaultLifecycleObserver {

    /**
     * Gọi khi máy thiếu giọng đọc của ngôn ngữ (AC-010.2).
     */
    public interface OnUnavailable {
        void onUnavailable();
    }

    private final Context context;
    private TextToSpeech engine;
    private boolean isReady;
    private Runnable pending;

    public TtsHelper(Context context, LifecycleOwner owner) {
        this.context = context.getApplicationContext();
        owner.getLifecycle().addObserver(this);
    }

    public void speak(String text, String localeTag, OnUnavailable onUnavailable) {
        Runnable action = () -> speakNow(text, Locale.forLanguageTag(localeTag), onUnavailable);
        if (isReady) {
            action.run();
            return;
        }
        pending = action;
        if (engine == null) {
            engine = new TextToSpeech(context, status -> {
                isReady = status == TextToSpeech.SUCCESS;
                if (!isReady) {
                    onUnavailable.onUnavailable();
                } else if (pending != null) {
                    pending.run();
                }
                pending = null;
            });
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        pending = null;
        if (engine != null) {
            engine.shutdown();
            engine = null;
        }
        isReady = false;
    }

    private void speakNow(String text, Locale locale, OnUnavailable onUnavailable) {
        int support = engine == null ? TextToSpeech.LANG_NOT_SUPPORTED : engine.setLanguage(locale);
        if (support == TextToSpeech.LANG_MISSING_DATA || support == TextToSpeech.LANG_NOT_SUPPORTED) {
            onUnavailable.onUnavailable();
        } else {
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, text);
        }
    }
}
