package org.hv.dbconnect;

import org.hv.Application;
import org.hv.demo.model.History;
import org.hv.pocket.identify.IncrementLongGenerator;
import org.hv.pocket.session.Session;
import org.hv.pocket.session.SessionFactory;
import org.hv.pocket.session.Transaction;
import org.hv.pocket.utils.PocketExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.util.concurrent.Executors;

/**
 * @author wujianchuan 2019/1/15
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MaxIdTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaxIdTest.class);

    @Autowired
    private IncrementLongGenerator incrementLongGenerator;
    private Session session;
    private Transaction transaction;

    @Before
    public void setup() {
        this.session = SessionFactory.getSession("homo");
        this.session.open();
        this.transaction = session.getTransaction();
        this.transaction.begin();
    }

    @After
    public void destroy() {
        this.transaction.commit();
        this.session.close();
    }

    @Test
    public void test1() {
        for (int i = 0; i < 2; i++) {
            Serializable maxId = incrementLongGenerator.getIdentify(History.class, session);
            LOGGER.info("max id >> {}", maxId);
        }
    }

    @Test
    public void test2() throws Exception {
        PocketExecutor.execute(Executors.newFixedThreadPool(20), 20, () -> LOGGER.info("max id >> {}", incrementLongGenerator.getIdentify(History.class, session)));
    }
}
