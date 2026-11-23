package com.learninge.app.data.remote;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.SyncRecord;
import com.learninge.proto.app.v1.Change;
import com.learninge.proto.app.v1.ChangeResult;
import com.learninge.proto.app.v1.PullRequest;
import com.learninge.proto.app.v1.PullResponse;
import com.learninge.proto.app.v1.PushRequest;
import com.learninge.proto.app.v1.PushResponse;
import com.learninge.proto.app.v1.SyncServiceGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Gọi {@code learninge.app.v1.SyncService}. Chạy trên luồng {@code io}.
 */
public class SyncRemote {
    private final GrpcClient client;

    public SyncRemote(GrpcClient client) {
        this.client = client;
    }

    /**
     * EP-11: kết quả cùng thứ tự với bản ghi đã gửi.
     */
    public Result<List<PushResult>> push(String deviceId, List<SyncRecord> records) {
        PushRequest request = PushRequest.newBuilder()
                .setDeviceId(deviceId)
                .addAllChanges(records.stream().map(SyncMapper::toProto).collect(Collectors.toList()))
                .build();
        Result<PushResponse> response = client.call(Rpc.PUSH, SyncServiceGrpc::newBlockingStub,
                stub -> stub.push(request));
        if (!response.isOk()) {
            return Result.errorOf(response);
        }
        if (response.value.getResultsCount() != records.size()) {
            return Result.error(ErrorCode.BAD_RESPONSE);
        }
        List<PushResult> results = new ArrayList<>();
        for (ChangeResult result : response.value.getResultsList()) {
            results.add(toResult(result));
        }
        return Result.ok(results);
    }

    /**
     * EP-12: một trang tối đa 500 bản ghi sau {@code since}.
     */
    public Result<PullPage> pull(long since) {
        PullRequest request = PullRequest.newBuilder().setSince(since).build();
        Result<PullResponse> response = client.call(Rpc.PULL, SyncServiceGrpc::newBlockingStub,
                stub -> stub.pull(request));
        if (!response.isOk()) {
            return Result.errorOf(response);
        }
        List<SyncRecord> records = response.value.getChangesList().stream().map(SyncMapper::fromProto)
                .filter(Objects::nonNull).collect(Collectors.toList());
        return Result.ok(new PullPage(records, response.value.getNextCursor(), response.value.getHasMore()));
    }

    private static PushResult toResult(ChangeResult result) {
        Change current = result.hasCurrent() ? result.getCurrent() : null;
        PushResult.Status status = switch (result.getStatus()) {
            case CHANGE_STATUS_APPLIED -> PushResult.Status.APPLIED;
            case CHANGE_STATUS_STALE -> PushResult.Status.STALE;
            case CHANGE_STATUS_DUPLICATE -> PushResult.Status.DUPLICATE;
            default -> PushResult.Status.REJECTED;
        };
        return new PushResult(status, result.getServerSeq(), current == null ? null : SyncMapper.fromProto(current),
                result.hasError() ? result.getError().getCode() : null);
    }
}
