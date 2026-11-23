package com.learninge.app.data.repo;

import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.data.dao.SyncStateDao;
import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.model.SyncRecord;
import com.learninge.app.data.model.SyncState;
import com.learninge.app.data.remote.PullPage;
import com.learninge.app.data.remote.PushResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Các giao dịch của một lượt đồng bộ. Phương thức ghi gọi trên {@code dbWrite}.
 */
public final class SyncRepository {
    private final DatabaseHelper database;
    private final SyncOutbox outbox;
    private final SyncInbox inbox;
    private final SyncStateDao state;
    private final ProgressRepository progress;

    public SyncRepository(DatabaseHelper database) {
        this.database = database;
        outbox = new SyncOutbox(database);
        inbox = new SyncInbox(database);
        state = new SyncStateDao(database);
        progress = new ProgressRepository(database);
    }

    public SyncState state(long userId) {
        return state.find(userId);
    }

    public int countPending(long userId) {
        return state.countDirty(userId);
    }

    public List<SyncRecord> nextBatch(long userId, int limit) {
        return outbox.next(userId, limit);
    }

    /**
     * Ghi kết quả một lô đã đẩy; trả mã lỗi của bản ghi bị từ chối đầu tiên, null nếu không có.
     */
    public String applyPush(long userId, List<SyncRecord> sent, List<PushResult> results) {
        String rejected = null;
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < sent.size(); i++) {
                PushResult result = results.get(i);
                if (result.status() == PushResult.Status.REJECTED && rejected == null) {
                    rejected = result.errorCode() == null ? "REJECTED" : result.errorCode();
                }
                // Bản bị từ chối cũng xóa cờ, để không thử lại vô hạn (BR-064).
                outbox.confirm(userId, sent.get(i), result.serverSeq());
                if (result.current() != null) {
                    inbox.apply(userId, result.current(), result.status() == PushResult.Status.STALE);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return rejected;
    }

    /**
     * Ghi một trang kéo về cùng mốc mới trong một giao dịch (BR-063).
     *
     * @param waiting bản ghi của các trang trước còn thiếu cha; bản nào vẫn thiếu thì ở lại danh sách
     */
    public void applyPull(long userId, PullPage page, List<SyncRecord> waiting) {
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        try {
            List<SyncRecord> records = new ArrayList<>(page.records());
            records.addAll(waiting);
            waiting.clear();
            // Bản ghi con có thể đứng trước cha trong cùng trang nên duyệt tới khi không ghi thêm được.
            for (int before = -1; before != records.size(); ) {
                before = records.size();
                records.removeIf(record -> inbox.apply(userId, record, false));
            }
            waiting.addAll(records);
            state.saveCursor(userId, page.nextCursor());
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void queueMissingNotebook(long userId) {
        outbox.queueMissingNotebook(userId);
    }

    /**
     * Kết thúc lượt: tính lại chuỗi ngày từ nhật ký đã hợp nhất (BR-031) và ghi kết quả cho SCR-35.
     *
     * @param errorCode null = thành công
     */
    public void finish(long userId, String errorCode, long now) {
        progress.refreshStreak(userId);
        if (errorCode == null) {
            state.saveSuccess(userId, now);
        } else {
            state.saveError(userId, errorCode, now);
        }
    }

    /**
     * Bản ghi bị từ chối không làm hỏng lượt đồng bộ nhưng vẫn hiện ở "Lỗi gần nhất".
     */
    public void saveRejected(long userId, String errorCode, long now) {
        state.saveError(userId, errorCode, now);
    }
}
