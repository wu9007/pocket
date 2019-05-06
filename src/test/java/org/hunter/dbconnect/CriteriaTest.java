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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
        Criteria criteria = this.session.createCriteria(Order.class);
        criteria.add(Restrictions.like("code", "%A%"))
                .add(Restrictions.or(Restrictions.gt("price", 13), Restrictions.lt("price", 12.58)))
                .add(Sort.asc("code"));
        List<Order> orderList = criteria.list();
        orderList.forEach(order -> System.out.println(order.getPrice()));
    }

    @Test
    public void test2() throws SQLException {
        Order order = Order.newInstance("C-001", new BigDecimal("50.25"));
        order.setDay(LocalDate.now());
        order.setTime(LocalDateTime.now());
        order.setState(false);
        order.setType("001");
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
    public void test4() throws SQLException {
        Order order = Order.newInstance("C-001", new BigDecimal("50.25"));
        order.setDay(LocalDate.now());
        order.setTime(LocalDateTime.now());
        this.session.save(order);
        Order newOrder = (Order) this.session.findOne(Order.class, order.getUuid());
        newOrder.setPrice(newOrder.getPrice().multiply(new BigDecimal("1.5")));
        this.session.update(newOrder);
        Criteria criteria = this.session.createCriteria(Order.class);
        criteria.add(Restrictions.lt("time", new Date()));
        List orderList = criteria.list();
    }

    @Test
    public void test5() throws SQLException {
        Order order = (Order) this.session.findDirect(Order.class, 11L);
        if (order != null) {
            System.out.println(order.getPrice());
        }
    }

    @Test
    public void test6() throws SQLException {
        Criteria criteria = this.session.createCriteria(Order.class);
        criteria.add(Restrictions.equ("uuid", 11L));
        Order order = (Order) criteria.unique(true);
        if (order != null) {
            System.out.println(order.getCommodities().size());
        }
    }

    @Test
    public void test8() throws SQLException {
        Criteria criteria = this.session.createCriteria(Order.class);
        criteria.add(Modern.set("price", 500.5D))
                .add(Restrictions.equ("code", "C-001"))
                .add(Modern.set("day", new Date()));
        System.out.println(criteria.update());
    }

    @Test
    public void test9() throws SQLException {
        Criteria criteria = this.session.createCriteria(Order.class);
        criteria.add(Restrictions.equ("code", "C-001"));
        System.out.println(criteria.max("typeName"));

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
    public void test11() throws SQLException {
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
    public void test14() throws SQLException {
        Criteria criteria = session.createCriteria(Order.class);
        criteria.add(Restrictions.equ("uuid", 1011011L));
        criteria.delete();
    }

    @Test
    public void test15() throws InterruptedException, SQLException, IllegalAccessException {
        ExecutorService executor = Executors.newFixedThreadPool(500);
        Order order = new Order();
        order.setCode("F-00x");
        order.setType("001");
        order.setTime(LocalDateTime.now());
        order.setPrice(new BigDecimal("99.56789"));
        order.setDay(LocalDate.now());
        this.session.save(order);
        PocketExecutor.execute(executor, 500, () -> {
            Order repositoryOrder = null;
            try {
                repositoryOrder = (Order) session.findOne(Order.class, order.getUuid());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println(repositoryOrder.getCode() + "=======================");
        });
        this.session.delete(order);
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

    @Test
    public void test17() throws SQLException {
        System.out.println(this.session.createCriteria(Order.class)
                .add(Restrictions.equ("code", "A-002")).count());
        this.session.createCriteria(Order.class).add(Restrictions.equ("code", "A-002")).delete();
        System.out.println(this.session.createCriteria(Order.class)
                .add(Restrictions.equ("code", "A-002")).count());
    }

    @Test
    public void test18() throws SQLException {
        session.createCriteria(Order.class)
                .add(Modern.setWithPoEl("#code  = CONCAT_WS('', #code, :STR_VALUE)"))
                .add(Modern.setWithPoEl("#price  = #price + :ADD_PRICE"))
                .add(Restrictions.equ("uuid", "10"))
                .setParameter("STR_VALUE", " - A")
                .setParameter("ADD_PRICE", 100)
                .update();
    }

    @Test
    public void test19() throws SQLException {
        Criteria criteria = this.session.createCriteria(Order.class)
                .add(Restrictions.equ("state", false));
        long count = criteria.count();
        System.out.println(count);
    }

    @Test
    public void test20() throws SQLException {
        Criteria criteria = this.session.createCriteria(Order.class)
                .add(Restrictions.equ("state", true));
        long count = criteria.delete();
        System.out.println(count);
    }

    @Test
    public void test21() throws SQLException {
        Criteria criteria = this.session.createCriteria(Order.class);
        criteria.add(Restrictions.like("type", "001"));
        List<Order> orders = criteria.list(true);
        System.out.println(orders.size());
    }

    @Test
    public void test22() throws SQLException {
        Criteria criteria = this.session.createCriteria(Order.class);
        criteria.add(Restrictions.equ("typeName", "手机支付"))
                .add(Restrictions.equ("type", "001"))
                .add(Sort.asc("typeName"))
                .add(Sort.asc("type"))
                .limit(0, 5);
        List<Order> orders = criteria.listNotCleanRestrictions();
        System.out.println(orders.size());
        System.out.println(criteria.count());
    }

    @Test
    public void test23() throws SQLException {
        Criteria criteria = this.session.createCriteria(Order.class);
        criteria.add(Modern.set("type", "001"))
                .update();
    }

    @Test
    public void test24() throws SQLException {
        Criteria criteria = this.session.createCriteria(Order.class);
        List<Order> orders = criteria.add(Restrictions.isNull("state"))
                .add(Restrictions.equ("code", "C-001"))
                .list();
        System.out.println(orders);
    }

    @Test
    public void test25() throws SQLException {
        Criteria criteria = this.session.createCriteria(Order.class);
        List<String> types = Arrays.asList("002", "003", "004");
        List<Order> orders = criteria.add(Restrictions.in("type", types))
                .list();
        System.out.println(orders);
    }
}
