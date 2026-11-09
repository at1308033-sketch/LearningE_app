package vn.edu.hcmute.damh.app;

import android.app.Application;
import android.os.StrictMode;

import vn.edu.hcmute.damh.BuildConfig;

/**
 * Lớp Application của DAMH English.
 * T-005 sẽ khởi tạo ServiceLocator (DatabaseHelper, AppExecutors, Repository, Service) tại đây.
 */
public class DamhApp extends Application {

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
