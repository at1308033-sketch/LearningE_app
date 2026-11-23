package com.learninge.app.data.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.DbContract.Sync;
import com.learninge.app.data.db.DbContract.XpEvents;
import com.learninge.app.data.model.XpSource;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Bảng {@code xp_events}: chỉ thêm; {@code xp_awarded} do server ghi đè khi đồng bộ.
 */
public final class XpDao {
    private final DatabaseHelper database;

    public XpDao(DatabaseHelper database) {
        this.database = database;
    }

    public void insert(long userId, XpSource source, String refUuid, int correct, int wrong, int xp, LocalDate day,
                       long now) {
        ContentValues values = new ContentValues();
        values.put(Sync.UUID, UUID.randomUUID().toString());
        values.put(XpEvents.USER_ID, userId);
        values.put(XpEvents.SOURCE_TYPE, source.name());
        values.put(XpEvents.REF_UUID, refUuid);
        values.put(XpEvents.CORRECT_COUNT, correct);
        values.put(XpEvents.WRONG_COUNT, wrong);
        values.put(XpEvents.TOTAL_COUNT, correct + wrong);
        values.put(XpEvents.XP_LOCAL, xp);
        values.put(XpEvents.OCCURRED_AT, now);
        values.put(XpEvents.LOCAL_DATE, day.toString());
        values.put(Sync.UPDATED_AT, now);
        database.getWritableDatabase().insertOrThrow(XpEvents.TABLE, null, values);
    }

    /**
     * Tổng XP từ một ngày trở đi; ưu tiên số server đã tính.
     */
    public int sumFrom(long userId, LocalDate day) {
        try (Cursor cursor = database.getReadableDatabase().rawQuery("SELECT COALESCE(SUM(COALESCE(xp_awarded, "
                        + "xp_local)), 0) FROM xp_events WHERE user_id = ? AND local_date >= ?",
                new String[]{String.valueOf(userId), day.toString()})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }
}
