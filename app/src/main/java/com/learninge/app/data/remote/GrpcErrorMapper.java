package com.learninge.app.data.remote;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;

import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;

/**
 * (mã gRPC, trailer {@code error-code}) → {@link ErrorCode} (SYSTEM-BLUEPRINT §Mô hình lỗi).
 */
final class GrpcErrorMapper {
    private static final Metadata.Key<String> ERROR_CODE = ascii("error-code");
    private static final Metadata.Key<String> RETRY_AFTER = ascii("retry-after-seconds");
    private static final Metadata.Key<String> ERROR_FIELDS = ascii("error-fields");

    private GrpcErrorMapper() {
    }

    /**
     * {@code detail} là số giây phải chờ, hoặc trường sai ("email=INVALID"), tùy loại lỗi.
     */
    static <T> Result<T> toResult(StatusRuntimeException failure) {
        String reason = trailer(failure, ERROR_CODE);
        String fields = trailer(failure, ERROR_FIELDS);
        return Result.error(map(failure, reason), fields != null ? fields : trailer(failure, RETRY_AFTER));
    }

    static int retryAfterSeconds(StatusRuntimeException failure) {
        return Result.error(ErrorCode.RATE_LIMITED, trailer(failure, RETRY_AFTER)).detailAsInt(0);
    }

    private static ErrorCode map(StatusRuntimeException failure, String reason) {
        switch (failure.getStatus().getCode()) {
            case INVALID_ARGUMENT:
                return ErrorCode.VALIDATION;
            case UNAUTHENTICATED:
                return oneOf(reason, ErrorCode.UNAUTHENTICATED, ErrorCode.INVALID_CREDENTIALS,
                        ErrorCode.INVALID_SECURITY_ANSWER);
            case PERMISSION_DENIED:
                return oneOf(reason, ErrorCode.SERVER_UNAVAILABLE, ErrorCode.INVALID_CREDENTIALS,
                        ErrorCode.ACCOUNT_DELETED);
            case ALREADY_EXISTS:
                return oneOf(reason, ErrorCode.DUPLICATE, ErrorCode.EMAIL_TAKEN, ErrorCode.NICKNAME_TAKEN);
            case RESOURCE_EXHAUSTED:
                return oneOf(reason, ErrorCode.RATE_LIMITED, ErrorCode.ACCOUNT_LOCKED, ErrorCode.RESET_LOCKED);
            case NOT_FOUND:
                return ErrorCode.NOT_FOUND;
            case FAILED_PRECONDITION:
                return oneOf(reason, ErrorCode.SERVER_UNAVAILABLE, ErrorCode.UPGRADE_REQUIRED);
            case DEADLINE_EXCEEDED:
                return ErrorCode.NETWORK_TIMEOUT;
            case UNAVAILABLE:
                // Không có trailer nghĩa là chưa tới được server.
                return reason == null ? ErrorCode.NETWORK_OFFLINE : ErrorCode.SERVER_UNAVAILABLE;
            case UNIMPLEMENTED:
                return ErrorCode.BAD_RESPONSE;
            default:
                return ErrorCode.SERVER_UNAVAILABLE;
        }
    }

    private static ErrorCode oneOf(String reason, ErrorCode fallback, ErrorCode... accepted) {
        for (ErrorCode code : accepted) {
            if (code.name().equals(reason)) {
                return code;
            }
        }
        return fallback;
    }

    private static String trailer(StatusRuntimeException failure, Metadata.Key<String> key) {
        Metadata trailers = failure.getTrailers();
        return trailers == null ? null : trailers.get(key);
    }

    private static Metadata.Key<String> ascii(String name) {
        return Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
    }
}
