package com.learninge.app.data.remote;

import com.google.protobuf.CodedOutputStream;
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
    // Máy chủ nhận tối đa 1 MiB mỗi message; chừa chỗ cho phần đầu gói.
    private static final int MAX_PUSH_BYTES = 900 * 1024;

    private final GrpcClient client;

    public SyncRemote(GrpcClient client) {
        this.client = client;
    }

    /**
     * Các bản ghi đầu danh sách vừa một lần đẩy, luôn có ít nhất một bản ghi nếu danh sách không rỗng.
     */
    public static List<SyncRecord> firstFitting(List<SyncRecord> records) {
        int bytes = 0;
        for (int i = 0; i < records.size(); i++) {
            bytes += CodedOutputStream.computeMessageSize(PushRequest.CHANGES_FIELD_NUMBER,
                    SyncMapper.toProto(records.get(i)));
            if (bytes > MAX_PUSH_BYTES && i > 0) {
                return records.subList(0, i);
            }
        }
        return records;
    }

    /**
     * EP-11.
     */
    public Result<PushReply> push(String deviceId, List<SyncRecord> records) {
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
        return Result.ok(new PushReply(results, response.value.getDataEpoch()));
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
        return Result.ok(new PullPage(records, response.value.getNextCursor(), response.value.getHasMore(),
                response.value.getDataEpoch()));
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
