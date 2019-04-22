package org.hunter.dbconnect;

import org.hunter.Application;
import org.hunter.demo.model.Order;
import org.hunter.pocket.session.Session;
import org.hunter.pocket.session.SessionFactory;
import org.hunter.pocket.session.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;

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
        Order order = new Order();
        order.setCode("F-00x");
        order.setType("001");
        order.setTime(new Date());
        order.setPrice(new BigDecimal("99.56789"));
        order.setDay(new Date());
        this.session.saveVariable(order);
        this.session.save(order);
    }

    @Test
    public void test2() throws SQLException {
        Order order = (Order) session.findOne(Order.class, 10110180);
        order.setState(null);
        order.setPrice(new BigDecimal("100.96"));
        order.setDay(new Date());
        order.setType("002");
        session.update(order);
    }

    @Test
    public void test3() throws SQLException {
        Order order = (Order) session.findOne(Order.class, 10110180);
        session.delete(order);
    }

    @Test
    public void test4() throws Exception {
        long uuid = session.getMaxUuid(101, Order.class);
        System.out.println(uuid);
    }
}
