package com.learninge.app.ui.me;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.Statistics;
import com.learninge.app.ui.common.BaseViewModel;

/**
 * Số liệu màn Tiến độ.
 */
public class ProgressViewModel extends BaseViewModel {
    private final MutableLiveData<Result<Statistics>> statistics = new MutableLiveData<>();

    public ProgressViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Result<Statistics>> statistics() {
        return statistics;
    }

    void load() {
        services.executors().dbRead().execute(() -> statistics.postValue(services.statisticsService().load()));
    }
}
