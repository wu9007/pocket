package org.hv.dbconnect;

import org.hv.Application;
import org.hv.demo.model.RelevantBill;
import org.hv.pocket.session.Session;
import org.hv.pocket.session.SessionFactory;
import org.hv.pocket.session.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/15
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SessionTest {

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
    public void test1() throws SQLException {
        RelevantBill order = (RelevantBill) this.session.findOne(RelevantBill.class, "10130");
        System.out.println(order.getCode());
        order.setCode("Hello-001");
        this.session.update(order);
        System.out.println(order.getCode());
    }
}
