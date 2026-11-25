package com.learninge.app.data.remote;

import static org.junit.Assert.assertEquals;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;

import org.junit.Test;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class GrpcErrorMapperTest {

    @Test
    public void unavailable_withoutTrailer_isOffline() {
        assertEquals(ErrorCode.NETWORK_OFFLINE, map(Status.UNAVAILABLE));
        assertEquals(ErrorCode.SERVER_UNAVAILABLE, map(Status.UNAVAILABLE, "error-code", "SERVICE_UNAVAILABLE"));
    }

    @Test
    public void wrongPassword_dependsOnStatus() {
        assertEquals(ErrorCode.INVALID_CREDENTIALS, map(Status.UNAUTHENTICATED, "error-code", "INVALID_CREDENTIALS"));
        // Đổi mật khẩu sai không được làm mất phiên (EP-07).
        assertEquals(ErrorCode.INVALID_CREDENTIALS, map(Status.PERMISSION_DENIED, "error-code", "INVALID_CREDENTIALS"));
        assertEquals(ErrorCode.UNAUTHENTICATED, map(Status.UNAUTHENTICATED, "error-code", "UNAUTHENTICATED"));
        assertEquals(ErrorCode.ACCOUNT_DELETED, map(Status.PERMISSION_DENIED, "error-code", "ACCOUNT_DELETED"));
    }

    @Test
    public void taken_mapsToField() {
        assertEquals(ErrorCode.EMAIL_TAKEN, map(Status.ALREADY_EXISTS, "error-code", "EMAIL_TAKEN"));
        assertEquals(ErrorCode.NICKNAME_TAKEN, map(Status.ALREADY_EXISTS, "error-code", "NICKNAME_TAKEN"));
    }

    @Test
    public void locked_keepsRetryAfter() {
        Result<Void> result = GrpcErrorMapper.toResult(failure(Status.RESOURCE_EXHAUSTED, "error-code",
                "ACCOUNT_LOCKED", "retry-after-seconds", "60"));
        assertEquals(ErrorCode.ACCOUNT_LOCKED, result.error);
        assertEquals(60, result.detailAsInt(0));
        assertEquals(ErrorCode.RATE_LIMITED, map(Status.RESOURCE_EXHAUSTED, "error-code", "RATE_LIMITED"));
        assertEquals(ErrorCode.PAYLOAD_TOO_LARGE, map(Status.RESOURCE_EXHAUSTED, "error-code", "PAYLOAD_TOO_LARGE"));
    }

    @Test
    public void validation_keepsFields() {
        Result<Void> result = GrpcErrorMapper.toResult(failure(Status.INVALID_ARGUMENT, "error-code",
                "VALIDATION_ERROR", "error-fields", "email=INVALID"));
        assertEquals(ErrorCode.VALIDATION, result.error);
        assertEquals("email=INVALID", result.detail);
    }

    @Test
    public void otherCodes() {
        assertEquals(ErrorCode.NETWORK_TIMEOUT, map(Status.DEADLINE_EXCEEDED));
        assertEquals(ErrorCode.UPGRADE_REQUIRED, map(Status.FAILED_PRECONDITION, "error-code", "UPGRADE_REQUIRED"));
        assertEquals(ErrorCode.BAD_RESPONSE, map(Status.UNIMPLEMENTED));
        assertEquals(ErrorCode.SERVER_UNAVAILABLE, map(Status.INTERNAL, "error-code", "INTERNAL_ERROR"));
        assertEquals(ErrorCode.NOT_FOUND, map(Status.NOT_FOUND, "error-code", "NOT_FOUND"));
    }

    private static ErrorCode map(Status status, String... trailers) {
        return GrpcErrorMapper.toResult(failure(status, trailers)).error;
    }

    static StatusRuntimeException failure(Status status, String... trailers) {
        Metadata metadata = new Metadata();
        for (int i = 0; i < trailers.length; i += 2) {
            metadata.put(Metadata.Key.of(trailers[i], Metadata.ASCII_STRING_MARSHALLER), trailers[i + 1]);
        }
        return status.asRuntimeException(metadata);
    }
}
