package org.hv.dbconnect;

import org.hv.Application;
import org.hv.PocketExecutor;
import org.hv.pocket.config.DatabaseConfig;
import org.hv.pocket.connect.ConnectionManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.Executors;

/**
 * @author wujianchuan 2019/1/16
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ConnectionTest {
    private static final int THREAD_NUM = 100;

    @Autowired
    DatabaseConfig databaseConfigs;

    @Test
    public void test1() throws InterruptedException {
        PocketExecutor.execute(Executors.newFixedThreadPool(THREAD_NUM), THREAD_NUM, () -> {
            ConnectionManager.getInstance().getConnection(databaseConfigs.getNode().get(0));
            ConnectionManager.getInstance().getConnection(databaseConfigs.getNode().get(1));
        });
        ConnectionManager.getInstance().destroy();
    }
}
