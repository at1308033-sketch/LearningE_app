package com.learninge.app.core;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Bốn luồng của ứng dụng (DEC-011). {@code dbWrite} chỉ có một luồng nên mọi thao tác ghi CSDL tự xếp hàng.
 */
public final class AppExecutors {
    private static final int DB_READ_THREADS = 2;
    private static final int IO_THREADS = 4;

    private final Executor main;
    private final ExecutorService dbRead;
    private final ExecutorService dbWrite;
    private final ExecutorService io;

    public AppExecutors() {
        Handler handler = new Handler(Looper.getMainLooper());
        main = handler::post;
        dbRead = Executors.newFixedThreadPool(DB_READ_THREADS);
        dbWrite = Executors.newSingleThreadExecutor();
        io = Executors.newFixedThreadPool(IO_THREADS);
    }

    public Executor main() {
        return main;
    }

    public ExecutorService dbRead() {
        return dbRead;
    }

    public ExecutorService dbWrite() {
        return dbWrite;
    }

    /**
     * Tệp SAF và mọi lời gọi gRPC.
     */
    public ExecutorService io() {
        return io;
    }
}
