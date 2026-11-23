package com.learninge.app.ui.leaderboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.Leaderboard;
import com.learninge.app.ui.common.BaseViewModel;

import java.time.Duration;
import java.time.Instant;

/**
 * Bảng xếp hạng tuần: hiện bản lưu ngay rồi tải mới.
 */
public class LeaderboardViewModel extends BaseViewModel {

    /**
     * @param board null nếu chưa có dữ liệu nào
     * @param error lỗi của lần tải gần nhất, null nếu thành công
     */
    record State(boolean isLoading, Leaderboard board, ErrorCode error) {
    }

    private final MutableLiveData<State> state = new MutableLiveData<>();

    public LeaderboardViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<State> state() {
        return state;
    }

    void load() {
        services.executors().io().execute(() -> {
            Result<Leaderboard> cached = services.leaderboardService().cached();
            state.postValue(new State(true, cached.value, null));
            Result<Leaderboard> fresh = services.leaderboardService().load();
            state.postValue(new State(false, fresh.isOk() ? fresh.value : cached.value, fresh.error));
        });
    }

    Duration untilWeekEnd(Leaderboard board) {
        Instant end = com.learninge.app.domain.rules.XpRules.weekEnd(board.weekStart());
        Duration left = Duration.between(services.clock().instant(), end);
        return left.isNegative() ? Duration.ZERO : left;
    }
}
