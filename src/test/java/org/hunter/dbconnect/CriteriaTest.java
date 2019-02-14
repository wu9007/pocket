package org.hunter.dbconnect;

import org.hunter.Application;
import org.hunter.pocket.connect.ConnectionManager;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
        start = System.currentTimeMillis();
        this.session = SessionFactory.getSession("homo");
        this.session.open();
        this.transaction = session.getTransaction();
        this.transaction.begin();
    }

    @After
    public void destroy() throws SQLException {
        this.transaction.commit();
        this.session.close();
        System.out.println("总耗时" + ((double) (System.currentTimeMillis() - this.start)) / 1000 + "秒");
    }

    @Test
    public void test1() throws Exception {
        Criteria criteria = this.session.creatCriteria(Order.class);
        criteria.add(Restrictions.like("code", "%A%"))
                .add(Restrictions.ne("code", "A-002"))
                .add(Restrictions.or(Restrictions.gt("price", 13), Restrictions.lt("price", 12.58)));
        List orderList = criteria.list();
        System.out.println(orderList.size());
    }

    @Test
    public void test2() throws Exception {
        Order order = Order.newInstance("C-001", new BigDecimal("50.25"));
        order.setDay(new Date());
        order.setTime(new Date());
        this.session.save(order);
        Order newOrder = (Order) this.session.findOne(Order.class, order.getUuid());
        System.out.println(newOrder.getDay());
    }

    @Test
    public void test3() throws Exception {
        Criteria criteria = this.session.creatCriteria(Order.class);
        criteria.add(Restrictions.lt("time", new Date()));
        List orderList = criteria.list(true);
        System.out.println(orderList.size());
    }

    @Test
    public void test4() throws Exception {
        Order order = Order.newInstance("C-001", new BigDecimal("50.25"));
        order.setDay(new Date());
        order.setTime(new Date());
        this.session.save(order);
        Order newOrder = (Order) this.session.findOne(Order.class, order.getUuid());
        newOrder.setPrice(newOrder.getPrice().multiply(new BigDecimal("1.5")));
        this.session.update(newOrder);
        Criteria criteria = this.session.creatCriteria(Order.class);
        criteria.add(Restrictions.lt("time", new Date()));
        List orderList = criteria.list();
    }

    @Test
    public void test5() throws Exception {
        Order order = (Order) this.session.findDirect(Order.class, 11L);
        System.out.println(order.getPrice());
    }

    @Test
    public void test6() throws Exception {
        Criteria criteria = this.session.creatCriteria(Order.class);
        criteria.add(Restrictions.equ("uuid", 11L));
        Order order = (Order) criteria.unique(true);
        System.out.println(order.getCommodities().size());
    }

    @Test
    public void test7() throws Exception {
        long s1 = System.currentTimeMillis();
        Order order = (Order) this.session.findOne(Order.class, 11L);
        long e1 = System.currentTimeMillis();
        System.out.println(e1 - s1 + "------" + order.getCode());

        long s2 = System.currentTimeMillis();
        Order order1 = (Order) this.session.findOne(Order.class, 11L);
        long e2 = System.currentTimeMillis();
        System.out.println(e2 - s2 + "------" + order1.getCode());


        long s3 = System.currentTimeMillis();
        Order order2 = (Order) this.session.findOne(Order.class, 11L);
        long e3 = System.currentTimeMillis();
        System.out.println(e3 - s3 + "------" + order2.getCode());

        this.session.removeCache(order);
        long s4 = System.currentTimeMillis();
        Order order4 = (Order) this.session.findOne(Order.class, 11L);
        long e4 = System.currentTimeMillis();
        System.out.println(e4 - s4 + "------" + order4.getCode());
    }

    @Test
    public void test8() throws Exception {
        Criteria criteria = this.session.creatCriteria(Order.class);
        criteria.add(Modern.set("price", 500.5D))
                .add(Restrictions.equ("code", "C-001"))
                .add(Modern.set("day", new Date()));
        System.out.println(criteria.update());
    }

    @Test
    public void test9() throws Exception {
        Criteria criteria = this.session.creatCriteria(Order.class);
        criteria.add(Restrictions.equ("code", "C-001"));
        System.out.println(criteria.max("price"));

        long count = criteria.add(Restrictions.like("code", "%001%"))
                .count();
        System.out.println(count);
    }

    @Test
    public void test10() throws Exception {
        Criteria criteria = this.session.creatCriteria(Order.class);
        List list = criteria.add(Restrictions.like("code", "%001%"))
                .add(Sort.desc("price"))
                .add(Sort.asc("uuid"))
                .list();
        System.out.println(list);
    }

    @Test
    public void test11() throws Exception {
        this.session.findDirect(Order.class, 11L);
        Order order = (Order) this.session.findOne(Order.class, 11L);
        order.setPrice(order.getPrice().add(new BigDecimal("20.1")));
        this.session.update(order);
    }

    @Test
    public void test12() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(100);
        for (int index = 0; index < 100; index++) {
            Thread thread = new Thread(() -> {
                try {
                    Order order = Order.newInstance("C-001", new BigDecimal("50.25"));
                    order.setDay(new Date());
                    order.setTime(new Date());
                    this.session.save(order);
                    Order newOrder = (Order) this.session.findOne(Order.class, order.getUuid());
                    System.out.println(newOrder.getDay());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            countDownLatch.countDown();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
