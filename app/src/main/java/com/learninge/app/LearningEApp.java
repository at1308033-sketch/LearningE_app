package com.learninge.app;

import android.app.Application;
import android.os.StrictMode;

/**
 * Lớp Application của LearningE.
 * T-005 sẽ khởi tạo ServiceLocator (DatabaseHelper, AppExecutors, Repository, Service) trong gói {@code core}.
 */
public class LearningEApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            // NFR-P2: phát hiện đọc/ghi đĩa trên luồng giao diện ở bản debug.
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .penaltyLog()
                    .build());
        }
    }
}
