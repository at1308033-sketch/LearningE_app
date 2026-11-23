package com.learninge.app.data.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.DbContract.Users;
import com.learninge.app.data.model.User;

/**
 * Bảng {@code users}: hồ sơ của các tài khoản đã đăng nhập trên máy.
 */
public final class UserDao {
    private static final String[] COLUMNS = {Users.ID, Users.SERVER_UUID, Users.EMAIL, Users.NICKNAME,
            Users.DISPLAY_NAME, Users.ONBOARDED};

    private final DatabaseHelper database;

    public UserDao(DatabaseHelper database) {
        this.database = database;
    }

    public User findById(long id) {
        return findOne(Users.ID + " = ?", String.valueOf(id));
    }

    public User findByServerUuid(String uuid) {
        return findOne(Users.SERVER_UUID + " = ?", uuid);
    }

    /**
     * Tài khoản còn dữ liệu trên máy (nhiều nhất một vì đăng nhập tài khoản khác sẽ xóa tài khoản cũ), hoặc null.
     */
    public User findLast() {
        return findOne(null);
    }

    /**
     * Thêm hoặc cập nhật hồ sơ theo mã máy chủ; trả khóa cục bộ.
     */
    public long save(String serverUuid, String email, String nickname, String displayName, long now) {
        ContentValues values = new ContentValues();
        values.put(Users.SERVER_UUID, serverUuid);
        values.put(Users.EMAIL, email);
        values.put(Users.NICKNAME, nickname);
        values.put(Users.DISPLAY_NAME, displayName);
        User existing = findByServerUuid(serverUuid);
        if (existing != null) {
            database.getWritableDatabase().update(Users.TABLE, values, Users.ID + " = ?",
                    new String[]{String.valueOf(existing.id())});
            return existing.id();
        }
        values.put(Users.CREATED_AT, now);
        return database.getWritableDatabase().insertOrThrow(Users.TABLE, null, values);
    }

    /**
     * Xóa người dùng cùng toàn bộ dữ liệu của họ trên máy (CASCADE).
     */
    public void delete(long id) {
        database.getWritableDatabase().delete(Users.TABLE, Users.ID + " = ?", new String[]{String.valueOf(id)});
    }

    private User findOne(String where, String... args) {
        try (Cursor cursor = database.getReadableDatabase().query(Users.TABLE, COLUMNS, where, args, null, null,
                Users.ID + " DESC", "1")) {
            return cursor.moveToFirst() ? new User(cursor.getLong(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getInt(5) == 1) : null;
        }
    }
}
