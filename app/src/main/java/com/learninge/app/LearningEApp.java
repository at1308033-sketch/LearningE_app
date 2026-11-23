package com.learninge.app;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.learninge.app.core.ServiceLocator;

/**
 * Lớp Application: tạo {@link ServiceLocator} dùng chung cho cả ứng dụng (DEC-001).
 */
public class LearningEApp extends Application {
    private ServiceLocator services;

    public static ServiceLocator services(Context context) {
        return ((LearningEApp) context.getApplicationContext()).services;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            enableStrictMode();
        }
        services = new ServiceLocator(this);
    }

    // NFR-P2: phát hiện đọc/ghi đĩa và gọi mạng trên luồng giao diện ở bản debug.
    private static void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build());
    }
}
