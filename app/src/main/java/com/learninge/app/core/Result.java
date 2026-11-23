package com.learninge.app.core;

import java.util.Objects;

/**
 * Kết quả của mọi Service: giá trị hoặc mã lỗi. Service không ném exception ra ngoài.
 */
public final class Result<T> {
    /**
     * null khi lỗi.
     */
    public final T value;
    /**
     * null khi thành công.
     */
    public final ErrorCode error;
    /**
     * Thông tin thêm của lỗi (tên trường, số giây còn khóa…), có thể null.
     */
    public final String detail;

    private Result(T value, ErrorCode error, String detail) {
        this.value = value;
        this.error = error;
        this.detail = detail;
    }

    public static <T> Result<T> ok(T value) {
        return new Result<>(value, null, null);
    }

    public static <T> Result<T> error(ErrorCode code) {
        return error(code, null);
    }

    public static <T> Result<T> error(ErrorCode code, String detail) {
        return new Result<>(null, Objects.requireNonNull(code), detail);
    }

    /**
     * Chuyển tiếp lỗi của kết quả khác kiểu.
     */
    public static <T> Result<T> errorOf(Result<?> failed) {
        return error(failed.error, failed.detail);
    }

    public boolean isOk() {
        return error == null;
    }

    /**
     * Số trong {@link #detail} (vd số giây còn khóa), hoặc giá trị mặc định nếu không phải số.
     */
    public int detailAsInt(int fallback) {
        if (detail == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(detail);
        } catch (NumberFormatException notANumber) {
            return fallback;
        }
    }
}
