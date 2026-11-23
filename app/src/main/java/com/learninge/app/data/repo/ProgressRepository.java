package com.learninge.app.data.repo;

import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.data.dao.SettingsDao;
import com.learninge.app.data.dao.StudyLogDao;
import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.model.ActivityType;
import com.learninge.app.domain.rules.StreakCalculator;
import com.learninge.app.domain.rules.StreakCalculator.Streak;

import java.time.LocalDate;

/**
 * Ghi hoạt động học và chuỗi ngày trong một giao dịch (NFR-A3).
 */
public final class ProgressRepository {
    private final DatabaseHelper database;
    private final StudyLogDao studyLog;
    private final SettingsDao settings;

    public ProgressRepository(DatabaseHelper database) {
        this.database = database;
        studyLog = new StudyLogDao(database);
        settings = new SettingsDao(database);
    }

    public Streak record(long userId, ActivityType type, String refUuid, LocalDate day, long now) {
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        try {
            studyLog.insert(userId, type, refUuid, day, now);
            Streak streak = refreshStreak(userId);
            db.setTransactionSuccessful();
            return streak;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Tính lại chuỗi từ toàn bộ nhật ký (cũng gọi sau mỗi lần kéo dữ liệu — BR-031).
     */
    public Streak refreshStreak(long userId) {
        Streak streak = streakOf(userId);
        settings.saveStreak(userId, streak);
        return streak;
    }

    public Streak streakOf(long userId) {
        return StreakCalculator.of(studyLog.listDays(userId));
    }

    public Long findLastDeckId(long userId) {
        return studyLog.findLastDeckId(userId);
    }
}
