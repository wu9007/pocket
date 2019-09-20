package org.hv;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * @author wujianchuan 2019/3/24
 * @version 1.0
 */
public class PocketExecutor {
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
        System.out.println("耗时：" + ((double)System.nanoTime() - startNanos) / 1000000000 + "秒");
    }
}
