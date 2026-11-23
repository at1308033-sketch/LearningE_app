package com.learninge.app.core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.VisibleForTesting;

import com.learninge.app.data.dao.CardProgressDao;
import com.learninge.app.data.dao.DeckDao;
import com.learninge.app.data.dao.SettingsDao;
import com.learninge.app.data.dao.StatisticsDao;
import com.learninge.app.data.dao.UserDao;
import com.learninge.app.data.dao.WordDao;
import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.SeedLoader;
import com.learninge.app.data.remote.AuthRemote;
import com.learninge.app.data.remote.GrpcClient;
import com.learninge.app.data.remote.NetworkMonitor;
import com.learninge.app.data.remote.ServerConfig;
import com.learninge.app.data.remote.TokenStore;
import com.learninge.app.data.repo.AccountRepository;
import com.learninge.app.data.repo.ProgressRepository;
import com.learninge.app.domain.AuthService;
import com.learninge.app.domain.DeckService;
import com.learninge.app.domain.ProgressService;
import com.learninge.app.domain.StatisticsService;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Nơi duy nhất tạo đối tượng dùng chung (DEC-001). Mọi thứ được tạo một lần khi ứng dụng khởi động.
 */
public final class ServiceLocator {
    private final Context context;
    private final AppExecutors executors = new AppExecutors();
    private final Clock fixedClock;
    private final SessionStore sessionStore;
    private final DatabaseHelper database;
    private final TokenStore tokenStore;
    private final NetworkMonitor networkMonitor;
    private final SessionEvents sessionEvents = new SessionEvents();
    private GrpcClient grpcClient;

    public ServiceLocator(Context context) {
        this(context, new DatabaseHelper(context), null);
    }

    private ServiceLocator(Context context, DatabaseHelper database, Clock fixedClock) {
        this.context = context.getApplicationContext();
        this.fixedClock = fixedClock;
        sessionStore = new SessionStore(context);
        // Chỉ mở tệp CSDL ở lần truy vấn đầu tiên, trên luồng nền.
        this.database = database;
        tokenStore = new TokenStore(context);
        networkMonitor = new NetworkMonitor(context);
    }

    /**
     * Dùng cho test: CSDL trong bộ nhớ và giờ cố định.
     */
    @VisibleForTesting
    public static ServiceLocator inMemory(Context context, Clock clock) {
        return new ServiceLocator(context, new DatabaseHelper(context, null), clock);
    }

    public AppExecutors executors() {
        return executors;
    }

    /**
     * Giờ của máy; đổi múi giờ khi app đang chạy thì ngày học theo múi giờ mới (BR-042).
     */
    public Clock clock() {
        return fixedClock != null ? fixedClock : Clock.system(ZoneId.systemDefault());
    }

    public SessionStore sessionStore() {
        return sessionStore;
    }

    public DatabaseHelper database() {
        return database;
    }

    public TokenStore tokenStore() {
        return tokenStore;
    }

    public SessionEvents sessionEvents() {
        return sessionEvents;
    }

    public NetworkMonitor networkMonitor() {
        return networkMonitor;
    }

    /**
     * Tạo ở lần gọi mạng đầu tiên; gọi trên luồng {@code io}.
     */
    public synchronized GrpcClient grpcClient() {
        if (grpcClient == null) {
            grpcClient = new GrpcClient(ServerConfig.load(context), tokenStore, networkMonitor, executors.io());
            grpcClient.setSessionListener(reason -> {
                authService().endSession(reason);
                sessionEvents.publish(reason);
            });
        }
        return grpcClient;
    }

    public AuthService authService() {
        return new AuthService(() -> new AuthRemote(grpcClient(), Build.MODEL), new AccountRepository(database),
                tokenStore, sessionStore, executors, clock());
    }

    public DeckService deckService() {
        return new DeckService(new DeckDao(database), new WordDao(database), new SettingsDao(database), sessionStore,
                clock());
    }

    public ProgressService progressService() {
        return new ProgressService(new ProgressRepository(database), new CardProgressDao(database),
                new DeckDao(database), new SettingsDao(database), new UserDao(database), sessionStore, clock());
    }

    public StatisticsService statisticsService() {
        return new StatisticsService(new StatisticsDao(database), new CardProgressDao(database),
                new ProgressRepository(database), deckService(), sessionStore, clock());
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
