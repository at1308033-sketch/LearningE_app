package com.learninge.app.domain;

import com.learninge.app.core.AppExecutors;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.core.SessionStore;
import com.learninge.app.data.dao.LeaderboardCacheDao;
import com.learninge.app.data.model.Leaderboard;
import com.learninge.app.data.remote.LeaderboardRemote;

import java.time.Clock;
import java.util.function.Supplier;

/**
 * Bảng xếp hạng tuần: tải mới, không được thì dùng bản đã lưu gần nhất (US-042, NFR-N1).
 */
public class LeaderboardService {
    private final Supplier<LeaderboardRemote> remote;
    private final LeaderboardCacheDao cache;
    private final SessionStore session;
    private final AppExecutors executors;
    private final Clock clock;

    public LeaderboardService(Supplier<LeaderboardRemote> remote, LeaderboardCacheDao cache, SessionStore session,
                              AppExecutors executors, Clock clock) {
        this.remote = remote;
        this.cache = cache;
        this.session = session;
        this.executors = executors;
        this.clock = clock;
    }

    /**
     * Gọi trên {@code io}. Lỗi mạng mà chưa có bản lưu thì trả lỗi đó.
     */
    public Result<Leaderboard> load() {
        Result<byte[]> fetched = remote.get().fetchWeekly();
        if (!fetched.isOk()) {
            Result<Leaderboard> cached = cached();
            return cached.isOk() && fetched.error != ErrorCode.UNAUTHENTICATED ? cached : Result.errorOf(fetched);
        }
        long now = clock.millis();
        Result<Leaderboard> board = LeaderboardRemote.parse(fetched.value, now, false);
        if (board.isOk()) {
            long userId = session.userId();
            executors.write(() -> {
                cache.save(userId, board.value.weekStart(), fetched.value, now);
                return null;
            });
        }
        return board;
    }

    /**
     * Bản đã lưu gần nhất; gọi trên {@code dbRead} hoặc {@code io}.
     */
    public Result<Leaderboard> cached() {
        LeaderboardCacheDao.Cached found = cache.findLatest(session.userId());
        return found == null ? Result.error(ErrorCode.NOT_FOUND)
                : LeaderboardRemote.parse(found.payload(), found.fetchedAt(), true);
    }
}
