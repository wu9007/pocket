package org.hunter.dbconnect;

import org.hunter.Application;
import org.hunter.pocket.config.DatabaseConfig;
import org.hunter.pocket.connect.ConnectionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;

/**
 * @author wujianchuan 2019/1/16
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ConnectionTest {
    private static final int THREAD_NUM = 100;
    private long start;

    @Autowired
    DatabaseConfig databaseConfigs;

    @Before
    public void setup() {
        start = System.nanoTime();
    }

    @After
    public void destroy() {
        System.out.println("耗时" + ((double) (System.nanoTime() - this.start)) / 1000000000 + "秒");
    }

    @Test
    public void test5() throws InterruptedException {
        CountDownLatch start = new CountDownLatch(THREAD_NUM);
        CountDownLatch done = new CountDownLatch(THREAD_NUM);
        for (int index = 0; index < THREAD_NUM; index++) {
            Thread thread = new Thread(() -> {
                try {
                    start.await();
                    ConnectionManager.getInstance().getConnection(databaseConfigs.getNode().get(0));
                    ConnectionManager.getInstance().getConnection(databaseConfigs.getNode().get(1));
                    done.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            start.countDown();
        }
        done.await();
        ConnectionManager.getInstance().destroy();
    }
}
