package com.learninge.app.ui.exam;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.ExamAttempt;
import com.learninge.app.ui.common.BaseViewModel;

import java.util.List;

/**
 * Các lượt thi đã nộp.
 */
public class ExamHistoryViewModel extends BaseViewModel {
    private final MutableLiveData<Result<List<ExamAttempt>>> attempts = new MutableLiveData<>();

    public ExamHistoryViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Result<List<ExamAttempt>>> attempts() {
        return attempts;
    }

    void load() {
        services.executors().dbRead().execute(() -> attempts.postValue(services.examService().history()));
    }
}
