package com.learninge.app.ui.exam;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.ExamSession;
import com.learninge.app.ui.common.BaseViewModel;

/**
 * Kết quả một lượt thi đã nộp.
 */
public class ExamResultViewModel extends BaseViewModel {
    private final MutableLiveData<Result<ExamSession>> session = new MutableLiveData<>();

    public ExamResultViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Result<ExamSession>> session() {
        return session;
    }

    void load(long attemptId) {
        services.executors().dbRead().execute(() -> session.postValue(services.examService().load(attemptId)));
    }
}
