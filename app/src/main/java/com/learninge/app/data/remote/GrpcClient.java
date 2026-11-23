package com.learninge.app.data.remote;

import com.learninge.app.BuildConfig;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.grpc.stub.AbstractBlockingStub;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Cửa duy nhất tới máy chủ: một {@link ManagedChannel} dùng chung, deadline từng RPC, thử lại, đổi lỗi sang
 * {@link ErrorCode}. Chỉ gọi trên luồng {@code io}.
 */
public class GrpcClient {
    private static final int MAX_MESSAGE_BYTES = 4 * 1024 * 1024;
    private static final int IDLE_MINUTES = 5;

    /**
     * Được báo khi server từ chối token (BR-059) hoặc tài khoản đã bị xóa.
     */
    public interface SessionListener {
        void onSessionEnded(ErrorCode reason);
    }

    private final ServerConfig config;
    private final TokenStore tokens;
    private final NetworkMonitor network;
    private final Executor io;
    private final RetryPolicy retry = new RetryPolicy(Thread::sleep, new Random());
    private final Map<Rpc, Long> blockedUntil = new EnumMap<>(Rpc.class);
    private ManagedChannel channel;
    private SessionListener sessionListener = reason -> {
    };

    public GrpcClient(ServerConfig config, TokenStore tokens, NetworkMonitor network, Executor io) {
        this.config = config;
        this.tokens = tokens;
        this.network = network;
        this.io = io;
        network.addOnAvailable(this::onNetworkAvailable);
    }

    public ServerConfig config() {
        return config;
    }

    public void setSessionListener(SessionListener listener) {
        sessionListener = listener;
    }

    /**
     * Gọi một RPC. {@code newStub} thường là {@code XxxServiceGrpc::newBlockingStub}.
     */
    public <S extends AbstractBlockingStub<S>, T> Result<T> call(Rpc rpc, Function<Channel, S> newStub,
                                                                 Function<S, T> body) {
        if (!network.isOnline()) {
            return Result.error(ErrorCode.NETWORK_OFFLINE);
        }
        long waitSeconds = blockedSeconds(rpc);
        if (waitSeconds > 0) {
            return Result.error(ErrorCode.RATE_LIMITED, String.valueOf(waitSeconds));
        }
        String token = rpc.isPublic ? null : tokens.token();
        if (!rpc.isPublic && token == null) {
            return Result.error(ErrorCode.UNAUTHENTICATED);
        }
        HeaderInterceptor headers = new HeaderInterceptor(BuildConfig.VERSION_CODE, token);
        try {
            return Result.ok(retry.run(rpc.isIdempotent, () -> body.apply(newStub.apply(channel())
                    .withInterceptors(headers)
                    .withDeadlineAfter(rpc.deadlineSeconds, TimeUnit.SECONDS))));
        } catch (StatusRuntimeException failure) {
            Result<T> error = GrpcErrorMapper.toResult(failure);
            onFailure(rpc, error, token != null);
            return error;
        }
    }

    private void onFailure(Rpc rpc, Result<?> error, boolean hadToken) {
        if (error.error == ErrorCode.RATE_LIMITED) {
            block(rpc, error.detailAsInt(0));
        } else if (hadToken && (error.error == ErrorCode.UNAUTHENTICATED
                || error.error == ErrorCode.ACCOUNT_DELETED)) {
            sessionListener.onSessionEnded(error.error);
        }
    }

    private synchronized void block(Rpc rpc, int seconds) {
        blockedUntil.put(rpc, System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds));
    }

    private synchronized long blockedSeconds(Rpc rpc) {
        Long until = blockedUntil.get(rpc);
        if (until == null) {
            return 0;
        }
        long nanos = until - System.nanoTime();
        return nanos <= 0 ? 0 : TimeUnit.NANOSECONDS.toSeconds(nanos) + 1;
    }

    private synchronized Channel channel() {
        if (channel == null) {
            OkHttpChannelBuilder builder = OkHttpChannelBuilder.forAddress(config.host(), config.port())
                    .maxInboundMessageSize(MAX_MESSAGE_BYTES)
                    .idleTimeout(IDLE_MINUTES, TimeUnit.MINUTES)
                    .executor(io)
                    // Thử lại chỉ ở RetryPolicy, không chồng hai lớp.
                    .disableRetry();
            if (config.isTls()) {
                builder.useTransportSecurity();
            } else if (PlaintextPolicy.isAllowed(BuildConfig.DEBUG, config.host(), ServerConfig.lanHosts())) {
                builder.usePlaintext();
            } else {
                throw new IllegalStateException("plaintext gRPC is not allowed for this build or host");
            }
            channel = builder.build();
        }
        return channel;
    }

    private synchronized void onNetworkAvailable() {
        if (channel != null) {
            channel.resetConnectBackoff();
        }
    }
}
