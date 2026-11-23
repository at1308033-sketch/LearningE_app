package com.learninge.app.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.Result;
import com.learninge.app.data.model.Leaderboard;
import com.learninge.app.data.model.TodaySummary;
import com.learninge.app.domain.rules.XpRules;
import com.learninge.app.sync.SyncStatus;
import com.learninge.app.ui.common.BaseViewModel;

import java.time.LocalTime;

/**
 * Số liệu hôm nay của Trang chủ.
 */
public class HomeViewModel extends BaseViewModel {
    /**
     * @param rank hạng trong bản đã lưu của tuần này, 0 nếu chưa biết
     */
    record WeekRank(int rank, int xp) {
    }

    private final MutableLiveData<WeekRank> weekRank = new MutableLiveData<>();
    private final MutableLiveData<Result<TodaySummary>> today = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Result<TodaySummary>> today() {
        return today;
    }

    LiveData<WeekRank> weekRank() {
        return weekRank;
    }

    LiveData<SyncStatus> syncStatus() {
        return services.syncManager().status();
    }

    void load() {
        services.executors().dbRead().execute(() -> {
            today.postValue(services.progressService().today());
            Result<Leaderboard> cached = services.leaderboardService().cached();
            int rank = cached.isOk() && cached.value.weekStart().equals(XpRules.weekStart(services.clock().instant()))
                    ? cached.value.me().rank() : 0;
            weekRank.postValue(new WeekRank(rank, services.xpService().weekXp()));
        });
    }

    int hour() {
        return LocalTime.now(services.clock()).getHour();
    }
}
