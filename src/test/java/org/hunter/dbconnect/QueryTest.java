package org.hunter.dbconnect;

import org.hunter.Application;
import org.hunter.demo.model.Order;
import org.hunter.pocket.query.SQLQuery;
import org.hunter.pocket.session.Session;
import org.hunter.pocket.session.SessionFactory;
import org.hunter.pocket.session.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.List;

/**
 * @author wujianchuan 2019/1/15
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class QueryTest {

    private Session session;
    private Transaction transaction;

    @Before
    public void setup() throws SQLException {
        this.session = SessionFactory.getSession("homo");
        this.session.open();
        this.transaction = session.getTransaction();
        this.transaction.begin();
    }

    @After
    public void destroy() throws SQLException {
        this.transaction.commit();
        this.session.close();
    }

    @Test
    public void test1() {
        SQLQuery query = this.session.createSQLQuery("select uuid as uuid,code as code,price as price from tbl_order", Order.class);
        Order order = (Order) query.unique();
        System.out.println(order.getCode());
    }

    @Test
    public void test2() {
        SQLQuery query = this.session.createSQLQuery("select uuid,code,price from tbl_order", Order.class);
        List<Order> orders = query.list();
        orders.forEach(order -> System.out.println(order.getCode()));
    }

    @Test
    public void test3() {
        SQLQuery query = this.session.createSQLQuery("select uuid,code,price from tbl_order", Order.class);
        List<Order> orders = query.limit(0, 5).list();
        orders.forEach(order -> System.out.println(order.getCode()));
    }
}
