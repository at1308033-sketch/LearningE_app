package com.learninge.app.core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.VisibleForTesting;

import com.learninge.app.data.dao.CardProgressDao;
import com.learninge.app.data.dao.DeckDao;
import com.learninge.app.data.dao.ExamDao;
import com.learninge.app.data.dao.FavoriteDao;
import com.learninge.app.data.dao.LanguageDao;
import com.learninge.app.data.dao.LeaderboardCacheDao;
import com.learninge.app.data.dao.SettingsDao;
import com.learninge.app.data.dao.StatisticsDao;
import com.learninge.app.data.dao.UserDao;
import com.learninge.app.data.dao.WordDao;
import com.learninge.app.data.dao.XpDao;
import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.SeedLoader;
import com.learninge.app.data.io.SafFiles;
import com.learninge.app.data.remote.AuthRemote;
import com.learninge.app.data.remote.GrpcClient;
import com.learninge.app.data.remote.LeaderboardRemote;
import com.learninge.app.data.remote.NetworkMonitor;
import com.learninge.app.data.remote.ServerConfig;
import com.learninge.app.data.remote.SyncRemote;
import com.learninge.app.data.remote.TokenStore;
import com.learninge.app.data.repo.AccountRepository;
import com.learninge.app.data.repo.DeckRepository;
import com.learninge.app.data.repo.ExamRepository;
import com.learninge.app.data.repo.ProgressRepository;
import com.learninge.app.data.repo.StudyRepository;
import com.learninge.app.data.repo.SyncRepository;
import com.learninge.app.domain.AuthService;
import com.learninge.app.domain.DeckEditor;
import com.learninge.app.domain.DeckService;
import com.learninge.app.domain.ExamService;
import com.learninge.app.domain.ImportExportService;
import com.learninge.app.domain.LanguageRegistry;
import com.learninge.app.domain.LeaderboardService;
import com.learninge.app.domain.ProgressService;
import com.learninge.app.domain.StatisticsService;
import com.learninge.app.domain.StudyService;
import com.learninge.app.domain.XpService;
import com.learninge.app.sync.SyncManager;
import com.learninge.app.sync.SyncScheduler;

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
    private final LanguageRegistry languages;
    private final TokenStore tokenStore;
    private final NetworkMonitor networkMonitor;
    private final SessionEvents sessionEvents = new SessionEvents();
    private final SyncRepository syncRepository;
    private final SyncManager syncManager;
    private final SyncScheduler syncScheduler;
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
        languages = new LanguageRegistry(new LanguageDao(database));
        tokenStore = new TokenStore(context);
        networkMonitor = new NetworkMonitor(context);
        syncRepository = new SyncRepository(database);
        syncManager = new SyncManager(() -> new SyncRemote(grpcClient()), syncRepository, sessionStore, executors,
                clock());
        syncScheduler = new SyncScheduler(syncManager, syncRepository, sessionStore, networkMonitor, executors);
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

    public SyncRepository syncRepository() {
        return syncRepository;
    }

    public SyncManager syncManager() {
        return syncManager;
    }

    public SyncScheduler syncScheduler() {
        return syncScheduler;
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
        return new DeckService(new DeckDao(database), new WordDao(database), new SettingsDao(database),
                new FavoriteDao(database), languages, sessionStore, clock());
    }

    public DeckEditor deckEditor() {
        return new DeckEditor(new DeckDao(database), new WordDao(database), new DeckRepository(database),
                deckService(), sessionStore, clock());
    }

    public LanguageRegistry languages() {
        return languages;
    }

    public ProgressService progressService() {
        return new ProgressService(new ProgressRepository(database), new CardProgressDao(database),
                new DeckDao(database), new SettingsDao(database), new UserDao(database), sessionStore, clock());
    }

    public StatisticsService statisticsService() {
        return new StatisticsService(new StatisticsDao(database), new CardProgressDao(database),
                new ProgressRepository(database), deckService(), sessionStore, clock());
    }

    public StudyService studyService() {
        return new StudyService(new WordDao(database), new CardProgressDao(database), new StudyRepository(database),
                new StatisticsDao(database), new UserDao(database), deckService(), progressService(), xpService(),
                sessionStore, clock());
    }

    public XpService xpService() {
        return new XpService(new XpDao(database), sessionStore, clock());
    }

    public LeaderboardService leaderboardService() {
        return new LeaderboardService(() -> new LeaderboardRemote(grpcClient()), new LeaderboardCacheDao(database),
                sessionStore, executors, clock());
    }

    public ExamService examService() {
        return new ExamService(new ExamDao(database), new ExamRepository(database), progressService(), xpService(),
                sessionStore, clock());
    }

    public ImportExportService importExportService() {
        return new ImportExportService(new SafFiles(context), new DeckDao(database), new WordDao(database),
                new DeckRepository(database), deckService(), sessionStore, executors, clock());
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
