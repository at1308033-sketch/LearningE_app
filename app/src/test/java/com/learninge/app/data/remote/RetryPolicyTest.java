package com.learninge.app.data.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class RetryPolicyTest {
    private final List<Long> sleeps = new ArrayList<>();
    private final RetryPolicy policy = new RetryPolicy(sleeps::add, new Random(1));
    private int calls;

    @Test
    public void idempotent_retriesThreeTimesWithBackoff() {
        assertThrows(StatusRuntimeException.class, () -> policy.run(true, () -> fail(Status.UNAVAILABLE)));
        assertEquals(4, calls);
        assertBetween(1_600, 2_400, sleeps.get(0));
        assertBetween(3_200, 4_800, sleeps.get(1));
        assertBetween(6_400, 9_600, sleeps.get(2));
    }

    @Test
    public void idempotent_returnsFirstSuccess() {
        assertEquals("ok", policy.run(true, () -> calls < 2 ? fail(Status.DEADLINE_EXCEEDED) : "ok"));
        assertEquals(2, sleeps.size());
    }

    @Test
    public void notIdempotent_neverRetries() {
        assertThrows(StatusRuntimeException.class, () -> policy.run(false, () -> fail(Status.UNAVAILABLE)));
        assertEquals(1, calls);
    }

    @Test
    public void permanentErrors_areNotRetried() {
        assertThrows(StatusRuntimeException.class, () -> policy.run(true, () -> fail(Status.RESOURCE_EXHAUSTED)));
        assertThrows(StatusRuntimeException.class, () -> policy.run(true, () -> fail(Status.UNAUTHENTICATED)));
        assertEquals(2, calls);
    }

    @Test
    public void retryAfter_extendsDelay() {
        assertThrows(StatusRuntimeException.class, () -> policy.run(true, () -> {
            throw GrpcErrorMapperTest.failure(Status.UNAVAILABLE, "retry-after-seconds", "5");
        }));
        assertEquals(5_000L, (long) sleeps.get(0));
    }

    private String fail(Status status) {
        calls++;
        throw status.asRuntimeException();
    }

    private static void assertBetween(long min, long max, long value) {
        assertTrue(value + " not in " + min + ".." + max, value >= min && value <= max);
    }
}
