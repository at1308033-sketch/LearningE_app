package com.learninge.app.core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.SeedLoader;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Nơi duy nhất tạo đối tượng dùng chung (DEC-001). Mọi thứ được tạo một lần khi ứng dụng khởi động.
 */
public final class ServiceLocator {
    private final Context context;
    private final AppExecutors executors = new AppExecutors();
    private final SessionStore sessionStore;
    private final DatabaseHelper database;

    public ServiceLocator(Context context) {
        this.context = context.getApplicationContext();
        sessionStore = new SessionStore(context);
        // Chỉ mở tệp CSDL ở lần truy vấn đầu tiên, trên luồng nền.
        database = new DatabaseHelper(context);
    }

    public AppExecutors executors() {
        return executors;
    }

    /**
     * Giờ của máy; đổi múi giờ khi app đang chạy thì ngày học theo múi giờ mới (BR-042).
     */
    public Clock clock() {
        return Clock.system(ZoneId.systemDefault());
    }

    public SessionStore sessionStore() {
        return sessionStore;
    }

    public DatabaseHelper database() {
        return database;
    }

    /**
     * Đổi mỗi lần cài hoặc cập nhật APK, nên seed chỉ được đọc lại khi có thể đã thay đổi.
     */
    public String installStamp() {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return String.valueOf(info.lastUpdateTime);
        } catch (PackageManager.NameNotFoundException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    /**
     * Gọi trên luồng {@code dbWrite}.
     */
    public SeedLoader seedLoader() {
        return new SeedLoader(context, database.getWritableDatabase(), clock());
    }
}
