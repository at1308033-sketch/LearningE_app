package com.learninge.app.data.remote;

import com.google.protobuf.InvalidProtocolBufferException;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.Leaderboard;
import com.learninge.proto.app.v1.GetWeeklyRequest;
import com.learninge.proto.app.v1.GetWeeklyResponse;
import com.learninge.proto.app.v1.LeaderboardEntry;
import com.learninge.proto.app.v1.LeaderboardServiceGrpc;
import com.learninge.proto.app.v1.WeekSelector;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gọi {@code learninge.app.v1.LeaderboardService} (EP-13) và đọc bản đã lưu. Chạy trên luồng {@code io}.
 */
public class LeaderboardRemote {
    private final GrpcClient client;

    public LeaderboardRemote(GrpcClient client) {
        this.client = client;
    }

    /**
     * Bytes của phản hồi, để lưu nguyên vẹn.
     */
    public Result<byte[]> fetchWeekly() {
        GetWeeklyRequest request = GetWeeklyRequest.newBuilder().setWeek(WeekSelector.WEEK_SELECTOR_CURRENT).build();
        Result<GetWeeklyResponse> response = client.call(Rpc.GET_WEEKLY, LeaderboardServiceGrpc::newBlockingStub,
                stub -> stub.getWeekly(request));
        return response.isOk() ? Result.ok(response.value.toByteArray()) : Result.errorOf(response);
    }

    public static Result<Leaderboard> parse(byte[] payload, long fetchedAt, boolean isCached) {
        try {
            GetWeeklyResponse response = GetWeeklyResponse.parseFrom(payload);
            List<Leaderboard.Entry> entries = response.getEntriesList().stream().map(LeaderboardRemote::entry)
                    .collect(Collectors.toList());
            return Result.ok(new Leaderboard(LocalDate.parse(response.getWeekStart()), entries,
                    entry(response.getMe()), fetchedAt, isCached));
        } catch (InvalidProtocolBufferException | DateTimeException broken) {
            return Result.error(ErrorCode.BAD_RESPONSE);
        }
    }

    private static Leaderboard.Entry entry(LeaderboardEntry entry) {
        return new Leaderboard.Entry(entry.getRank(), entry.getNickname(), entry.getXp(), entry.getStreak(),
                entry.getIsMe());
    }
}
