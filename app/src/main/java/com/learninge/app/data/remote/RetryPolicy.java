package com.learninge.app.data.remote;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Vòng thử lại BR-077: 2 s, 4 s, 8 s (±20%), tối đa 3 lần, chỉ cho RPC idempotent.
 */
final class RetryPolicy {
    private static final long[] DELAYS_MILLIS = {2_000, 4_000, 8_000};
    private static final double JITTER = 0.2;

    /**
     * Tách ra để test không phải chờ thật.
     */
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    private final Sleeper sleeper;
    private final Random random;

    RetryPolicy(Sleeper sleeper, Random random) {
        this.sleeper = sleeper;
        this.random = random;
    }

    <T> T run(boolean isIdempotent, Supplier<T> call) {
        for (int attempt = 0; ; attempt++) {
            try {
                return call.get();
            } catch (StatusRuntimeException failure) {
                if (!isIdempotent || attempt == DELAYS_MILLIS.length || !isTransient(failure)) {
                    throw failure;
                }
                pause(attempt, failure);
            }
        }
    }

    private static boolean isTransient(StatusRuntimeException failure) {
        Status.Code code = failure.getStatus().getCode();
        return code == Status.Code.UNAVAILABLE || code == Status.Code.DEADLINE_EXCEEDED;
    }

    private void pause(int attempt, StatusRuntimeException failure) {
        double factor = 1 + JITTER * (2 * random.nextDouble() - 1);
        long delay = Math.round(DELAYS_MILLIS[attempt] * factor);
        try {
            sleeper.sleep(Math.max(delay, GrpcErrorMapper.retryAfterSeconds(failure) * 1_000L));
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw failure;
        }
    }
}
