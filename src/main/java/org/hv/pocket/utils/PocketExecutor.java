package org.hv.pocket.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * @author wujianchuan 2019/3/24
 * @version 1.0
 */
public class PocketExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PocketExecutor.class);

    public static void execute(Executor executor, int concurrency, final Runnable action) throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(concurrency);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(concurrency);
        for (int index = 0; index < concurrency; index++) {
            executor.execute(() -> {
                ready.countDown();
                try {
                    start.await();
                    action.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        long startNanos = System.nanoTime();
        start.countDown();
        done.await();
        LOGGER.info("耗时：{}ms", ((double) System.nanoTime() - startNanos) / 1000000);
    }
}
