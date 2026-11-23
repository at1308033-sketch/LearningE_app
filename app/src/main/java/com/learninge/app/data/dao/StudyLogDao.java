package com.learninge.app.data.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.DbContract.StudyLog;
import com.learninge.app.data.db.DbContract.Sync;
import com.learninge.app.data.model.ActivityType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Bảng {@code study_log}: chỉ thêm, là nguồn để tính chuỗi ngày.
 */
public final class StudyLogDao {
    private final DatabaseHelper database;

    public StudyLogDao(DatabaseHelper database) {
        this.database = database;
    }

    public void insert(long userId, ActivityType type, String refUuid, LocalDate day, long now) {
        ContentValues values = new ContentValues();
        values.put(Sync.UUID, UUID.randomUUID().toString());
        values.put(StudyLog.USER_ID, userId);
        values.put(StudyLog.ACTIVITY_TYPE, type.name());
        values.put(StudyLog.REF_UUID, refUuid);
        values.put(StudyLog.STUDY_DATE, day.toString());
        values.put(StudyLog.CREATED_AT, now);
        values.put(Sync.UPDATED_AT, now);
        database.getWritableDatabase().insertOrThrow(StudyLog.TABLE, null, values);
    }

    /**
     * Các ngày có học, của mọi máy đã đồng bộ.
     */
    public List<LocalDate> listDays(long userId) {
        List<LocalDate> days = new ArrayList<>();
        try (Cursor cursor = database.getReadableDatabase().rawQuery(
                "SELECT DISTINCT study_date FROM study_log WHERE user_id = ?", new String[]{String.valueOf(userId)})) {
            while (cursor.moveToNext()) {
                days.add(LocalDate.parse(cursor.getString(0)));
            }
        }
        return days;
    }

    /**
     * Bộ của phiên học gần nhất còn trên máy; null nếu chưa có.
     */
    public Long findLastDeckId(long userId) {
        try (Cursor cursor = database.getReadableDatabase().rawQuery("SELECT d.id FROM study_log s "
                + "JOIN decks d ON d.uuid = s.ref_uuid AND d.deleted = 0 WHERE s.user_id = ? "
                + "ORDER BY s.created_at DESC, s.id DESC LIMIT 1", new String[]{String.valueOf(userId)})) {
            return cursor.moveToFirst() ? Long.valueOf(cursor.getLong(0)) : null;
        }
    }
}
