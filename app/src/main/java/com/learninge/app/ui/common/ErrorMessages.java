package com.learninge.app.ui.common;

import android.content.Context;

import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;

/**
 * {@link ErrorCode} → câu hiện cho người dùng (API-SPECIFICATION §2). Không dựa vào thông điệp của máy chủ.
 */
public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static String of(Context context, ErrorCode code) {
        return context.getString(resource(code));
    }

    private static int resource(ErrorCode code) {
        switch (code) {
            case VALIDATION:
                return R.string.error_validation;
            case INVALID_CREDENTIALS:
                return R.string.error_invalid_credentials;
            case WRONG_PASSWORD:
                return R.string.error_wrong_password;
            case EMAIL_TAKEN:
                return R.string.register_error_email_taken;
            case NICKNAME_TAKEN:
                return R.string.register_error_nickname_taken;
            case NETWORK_OFFLINE:
                return R.string.error_network_offline;
            case NETWORK_TIMEOUT:
                return R.string.error_network_timeout;
            case SERVER_UNAVAILABLE:
            case BAD_RESPONSE:
                return R.string.error_server_unavailable;
            case RATE_LIMITED:
            case ACCOUNT_LOCKED:
            case RESET_LOCKED:
                return R.string.error_rate_limited;
            case UPGRADE_REQUIRED:
                return R.string.error_upgrade_required;
            case ACCOUNT_DELETED:
                return R.string.error_account_deleted;
            case UNAUTHENTICATED:
                return R.string.error_session_expired;
            case FILE_TOO_LARGE:
                return R.string.error_file_too_large;
            case FILE_FORMAT:
                return R.string.error_file_format;
            case NO_VALID_ROWS:
                return R.string.error_no_valid_rows;
            case IO:
                return R.string.error_io;
            case READ_ONLY:
                return R.string.deck_read_only;
            case NOT_FOUND:
                return R.string.error_not_found;
            default:
                return R.string.error_generic;
        }
    }
}
