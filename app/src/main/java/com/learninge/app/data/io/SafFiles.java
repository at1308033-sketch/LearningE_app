package com.learninge.app.data.io;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * Đọc/ghi tệp người dùng chọn qua Storage Access Framework. Gọi trên luồng {@code io}.
 */
public final class SafFiles {
    private static final int BUFFER = 8192;

    private final Context context;

    public SafFiles(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Nội dung UTF-8 của tệp; không đọc quá {@code maxBytes} byte.
     */
    public Result<String> readText(Uri uri, int maxBytes) {
        try (InputStream in = resolver().openInputStream(uri)) {
            if (in == null) {
                return Result.error(ErrorCode.IO);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER];
            for (int n = in.read(buffer); n > 0; n = in.read(buffer)) {
                out.write(buffer, 0, n);
                if (out.size() > maxBytes) {
                    return Result.error(ErrorCode.FILE_TOO_LARGE);
                }
            }
            return Result.ok(StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(out.toByteArray()))
                    .toString());
        } catch (CharacterCodingException notUtf8) {
            return Result.error(ErrorCode.FILE_FORMAT, "tệp không phải UTF-8");
        } catch (IOException | SecurityException failed) {
            return Result.error(ErrorCode.IO, failed.getMessage());
        }
    }

    /**
     * Ghi tệp vừa tạo; lỗi giữa chừng thì xóa để không còn tệp dở dang (AC-033.2).
     */
    public Result<Void> writeText(Uri uri, String text) {
        try (OutputStream out = resolver().openOutputStream(uri, "wt")) {
            if (out == null) {
                return Result.error(ErrorCode.IO);
            }
            out.write(text.getBytes(StandardCharsets.UTF_8));
            return Result.ok(null);
        } catch (IOException | SecurityException failed) {
            deleteQuietly(uri);
            return Result.error(ErrorCode.IO, failed.getMessage());
        }
    }

    public String displayName(Uri uri) {
        try (Cursor cursor = resolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getString(0);
            }
        }
        String last = uri.getLastPathSegment();
        return last == null ? "" : last;
    }

    private void deleteQuietly(Uri uri) {
        try {
            DocumentsContract.deleteDocument(resolver(), uri);
        } catch (IOException | RuntimeException cannotDelete) {
            // Nơi lưu không cho xóa: người dùng vẫn thấy thông báo lỗi xuất tệp.
            android.util.Log.w("SafFiles", "cannot delete partial file: " + cannotDelete.getClass().getSimpleName());
        }
    }

    private ContentResolver resolver() {
        return context.getContentResolver();
    }
}
