package org.hunter.dbconnect;

import org.hunter.Application;
import org.hunter.PocketExecutor;
import org.hunter.pocket.config.DatabaseConfig;
import org.hunter.pocket.criteria.Criteria;
import org.hunter.pocket.criteria.Modern;
import org.hunter.pocket.criteria.Restrictions;
import org.hunter.pocket.criteria.Sort;
import org.hunter.pocket.session.Session;
import org.hunter.pocket.session.SessionFactory;
import org.hunter.pocket.session.Transaction;
import org.hunter.demo.model.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wujianchuan 2019/1/15
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class CriteriaTest {

    private Session session;
    private Transaction transaction;
    private long start;

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
        Criteria criteria = this.session.createCriteria(Order.class);
        criteria.add(Restrictions.like("code", "%A%"))
                .add(Restrictions.ne("code", "A-002"))
                .add(Restrictions.or(Restrictions.gt("price", 13), Restrictions.lt("price", 12.58)));
        List orderList = criteria.list();
        System.out.println(orderList.size());
    }

    @Test
    public void test2() {
        Order order = Order.newInstance("C-001", new BigDecimal("50.25"));
        order.setDay(new Date());
        order.setTime(new Date());
        order.setState(false);
        this.session.save(order);
        Order newOrder = (Order) this.session.findOne(Order.class, order.getUuid());
        System.out.println(newOrder.getDay());
    }

    @Test
    public void test3() {
        Criteria criteria = this.session.createCriteria(Order.class);
        criteria.add(Restrictions.lt("time", new Date()));
        List orderList = criteria.list(true);
        System.out.println(orderList.size());
    }

    @Test
    public void test4() {
        Order order = Order.newInstance("C-001", new BigDecimal("50.25"));
        order.setDay(new Date());
        order.setTime(new Date());
        this.session.save(order);
        Order newOrder = (Order) this.session.findOne(Order.class, order.getUuid());
        newOrder.setPrice(newOrder.getPrice().multiply(new BigDecimal("1.5")));
        this.session.update(newOrder);
        Criteria criteria = this.session.createCriteria(Order.class);
        criteria.add(Restrictions.lt("time", new Date()));
        List orderList = criteria.list();
    }

    @Test
    public void test5() {
        Order order = (Order) this.session.findDirect(Order.class, 11L);
        if (order != null) {
            System.out.println(order.getPrice());
        }
    }

    @Test
    public void test6() {
        Criteria criteria = this.session.createCriteria(Order.class);
        criteria.add(Restrictions.equ("uuid", 11L));
        Order order = (Order) criteria.unique(true);
        if (order != null) {
            System.out.println(order.getCommodities().size());
        }
    }

    @Test
    public void test8() {
        Criteria criteria = this.session.createCriteria(Order.class);
        criteria.add(Modern.set("price", 500.5D))
                .add(Restrictions.equ("code", "C-001"))
                .add(Modern.set("day", new Date()));
        System.out.println(criteria.update());
    }

    @Test
    public void test9() {
        Criteria criteria = this.session.createCriteria(Order.class);
        criteria.add(Restrictions.equ("code", "C-001"));
        System.out.println(criteria.max("price"));

        long count = criteria.add(Restrictions.like("code", "%001%"))
                .count();
        System.out.println(count);
    }

    @Test
    public void test10() throws InterruptedException {
        PocketExecutor.execute(Executors.newFixedThreadPool(100), 100, () -> {
            Criteria criteria = this.session.createCriteria(Order.class);
            List list = criteria.add(Restrictions.like("code", "%001%"))
                    .add(Sort.desc("price"))
                    .add(Sort.asc("uuid"))
                    .list();
            System.out.println(list);
        });
    }

    @Test
    public void test11() {
        this.session.findDirect(Order.class, 11L);
        Order order = (Order) this.session.findOne(Order.class, 11L);
        if (order != null) {
            order.setPrice(order.getPrice().add(new BigDecimal("20.1")));
            this.session.update(order);
        }
    }

    @Autowired
    DatabaseConfig databaseConfig;

    @Test
    public void test14() {
        Criteria criteria = session.createCriteria(Order.class);
        criteria.add(Restrictions.equ("uuid", 1011011L));
        criteria.delete();
    }

    @Test
    public void test15() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(500);
        PocketExecutor.execute(executor, 500, () -> {
            Order order = (Order) session.findOne(Order.class, 1);
            System.out.println(order.getCode() + "=======================");
        });
        executor.shutdown();
    }

    @Test
    public void test16() {
        Criteria criteria = this.session.createCriteria(Order.class)
                .add(Restrictions.equ("code", "C-001"))
                .limit(0, 5);
        List list = criteria.list();
        list.size();
    }
}
