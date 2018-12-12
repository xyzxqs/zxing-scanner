package io.github.xyzxqs.zxingscanner.demo.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtils {
    private ThreadUtils() {

    }

    private static volatile Thread sMainThread;
    private static volatile Handler sMainThreadHandler;
    private static volatile ExecutorService sCachedThreadPoolExecutor;

    /**
     * Returns true if the current thread is the UI thread.
     */
    public static boolean isMainThread() {
        if (sMainThread == null) {
            sMainThread = Looper.getMainLooper().getThread();
        }
        return Thread.currentThread() == sMainThread;
    }

    /**
     * Returns a shared UI thread handler.
     */
    public static Handler getMainThreadHandler() {
        if (sMainThreadHandler == null) {
            sMainThreadHandler = new Handler(Looper.getMainLooper());
        }

        return sMainThreadHandler;
    }

    /**
     * Checks that the current thread is the UI thread. Otherwise throws an exception.
     */
    public static void ensureMainThread() {
        if (!isMainThread()) {
            throw new RuntimeException("Must be called on the UI thread");
        }
    }

    /**
     * Posts runnable in background using shared background thread pool.
     */
    public static void runOnBackgroundThread(Runnable runnable) {
        if (sCachedThreadPoolExecutor == null) {
            sCachedThreadPoolExecutor = Executors.newCachedThreadPool();
        }
        sCachedThreadPoolExecutor.execute(runnable);
    }

    public static void runOnMainThread(Runnable action) {
        if (isMainThread()) {
            action.run();
        }
        else {
            runOnMainThread(action, 0);
        }
    }

    public static void runOnMainThread(Runnable action, long delayMillis) {
        getMainThreadHandler().postDelayed(action, delayMillis);
    }
}
