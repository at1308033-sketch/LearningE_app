package com.learninge.app.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.TodaySummary;
import com.learninge.app.ui.common.BaseViewModel;

import java.time.LocalTime;

/**
 * Số liệu hôm nay của Trang chủ.
 */
public class HomeViewModel extends BaseViewModel {
    private final MutableLiveData<Result<TodaySummary>> today = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Result<TodaySummary>> today() {
        return today;
    }

    void load() {
        services.executors().dbRead().execute(() -> today.postValue(services.progressService().today()));
    }

    int hour() {
        return LocalTime.now(services.clock()).getHour();
    }
}
