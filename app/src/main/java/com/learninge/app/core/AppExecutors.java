package com.learninge.app.core;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

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
     * Chạy trên {@code dbWrite} và chờ kết quả. Chỉ gọi từ luồng {@code io} (vd ghi kết quả của một lời gọi mạng).
     */
    public <T> T write(Supplier<T> task) {
        try {
            return dbWrite.submit(task::get).get();
        } catch (ExecutionException failed) {
            if (failed.getCause() instanceof RuntimeException) {
                throw (RuntimeException) failed.getCause();
            }
            throw new IllegalStateException(failed.getCause());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interrupted);
        }
    }

    /**
     * Tệp SAF và mọi lời gọi gRPC.
     */
    public ExecutorService io() {
        return io;
    }
}
