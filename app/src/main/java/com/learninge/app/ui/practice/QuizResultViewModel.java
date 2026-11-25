package com.learninge.app.ui.practice;

import android.app.Application;

import androidx.annotation.NonNull;

import com.learninge.app.ui.common.BaseViewModel;

/**
 * SCR-14 — ViewModel kết quả quiz. Args truyền qua SafeArgs đủ, không cần state phức tạp.
 */
public class QuizResultViewModel extends BaseViewModel {
    public QuizResultViewModel(@NonNull Application application) {
        super(application);
    }
}
