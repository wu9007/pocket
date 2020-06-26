package org.hv.pocket.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author leyan95
 */
public enum EnumPocketThreadPool {
    /**
     *
     */
    INSTANCE;

    private final ExecutorService persistenceLogExecutorService;

    EnumPocketThreadPool() {
        this.persistenceLogExecutorService = new
                ThreadPoolExecutor(7, 50, 50L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(200), new ThreadFactoryBuilder()
                .setNameFormat("PersistenceLogExecutorService:thread-%d").build(), new ThreadPoolExecutor.AbortPolicy());
    }

    public ExecutorService getPersistenceLogExecutorService() {
        return persistenceLogExecutorService;
    }
}
