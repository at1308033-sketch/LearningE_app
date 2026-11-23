package com.learninge.app.data.repo;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.data.dao.SettingsDao;
import com.learninge.app.data.dao.SyncStateDao;
import com.learninge.app.data.dao.UserDao;
import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.DbContract.Decks;
import com.learninge.app.data.db.DbContract.Sync;
import com.learninge.app.data.model.User;
import com.learninge.app.data.remote.RemoteSession;
import com.learninge.app.domain.rules.NotebookId;
import com.learninge.app.domain.rules.TextNormalizer;

/**
 * Dữ liệu tài khoản trên máy; mỗi phương thức ghi là một giao dịch. Gọi trên luồng {@code dbWrite}.
 */
public final class AccountRepository {
    public static final String NOTEBOOK_NAME = "Sổ tay của tôi";

    private final DatabaseHelper database;
    private final UserDao users;
    private final SettingsDao settings;
    private final SyncStateDao syncState;

    public AccountRepository(DatabaseHelper database) {
        this.database = database;
        users = new UserDao(database);
        settings = new SettingsDao(database);
        syncState = new SyncStateDao(database);
    }

    /**
     * Lưu tài khoản vừa đăng nhập. Dữ liệu của tài khoản khác trên máy bị xóa (BR-059, đã hỏi người dùng trước đó).
     */
    public User signIn(RemoteSession session, long now) {
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        try {
            User last = users.findLast();
            if (last != null && !last.serverUuid().equals(session.userId())) {
                users.delete(last.id());
            }
            long userId = users.save(session.userId(), session.email(), session.nickname(), session.displayName(), now);
            settings.insertDefaults(userId);
            syncState.insertIfAbsent(userId);
            insertNotebook(db, userId, session);
            db.setTransactionSuccessful();
            return users.findById(userId);
        } finally {
            db.endTransaction();
        }
    }

    public User findById(long userId) {
        return users.findById(userId);
    }

    public User findLast() {
        return users.findLast();
    }

    public int countPending(long userId) {
        return syncState.countDirty(userId);
    }

    public void delete(long userId) {
        users.delete(userId);
    }

    // BR-046: server tự tạo bộ này khi đăng ký nên bản trên máy chưa cần đẩy; lần đồng bộ đầu kéo trước, nếu server
    // chưa có thì SyncOutbox.queueMissingNotebook mới đẩy.
    private static void insertNotebook(SQLiteDatabase db, long userId, RemoteSession session) {
        ContentValues values = new ContentValues();
        values.put(Sync.UUID, NotebookId.of(session.userId()));
        values.put(Decks.OWNER_USER_ID, userId);
        values.put(Decks.NAME, NOTEBOOK_NAME);
        values.put(Decks.NAME_NORM, TextNormalizer.normalize(NOTEBOOK_NAME));
        values.put(Decks.LANGUAGE_CODE, "en");
        values.put(Decks.IS_DEFAULT, 1);
        values.put(Decks.CREATED_AT, session.createdAtMillis());
        values.put(Sync.UPDATED_AT, session.createdAtMillis());
        values.put(Sync.DIRTY, 0);
        db.insertWithOnConflict(Decks.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }
}
