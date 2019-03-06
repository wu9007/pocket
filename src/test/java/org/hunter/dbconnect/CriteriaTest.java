package org.hunter.dbconnect;

import org.hunter.Application;
import org.hunter.pocket.config.DatabaseConfig;
import org.hunter.pocket.connect.ConnectionManager;
import org.hunter.pocket.connect.DatabaseManager;
import org.hunter.pocket.criteria.Criteria;
import org.hunter.pocket.criteria.Modern;
import org.hunter.pocket.criteria.Restrictions;
import org.hunter.pocket.criteria.Sort;
import org.hunter.pocket.query.ProcessQuery;
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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

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
        order.setState(false);
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
        if (order != null) {
            System.out.println(order.getPrice());
        }
    }

    @Test
    public void test6() throws Exception {
        Criteria criteria = this.session.creatCriteria(Order.class);
        criteria.add(Restrictions.equ("uuid", 11L));
        Order order = (Order) criteria.unique(true);
        if (order != null) {
            System.out.println(order.getCommodities().size());
        }
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
        if (order != null) {
            order.setPrice(order.getPrice().add(new BigDecimal("20.1")));
            this.session.update(order);
        }
    }

    @Autowired
    DatabaseConfig databaseConfig;

    @Test
    public void test12() throws Exception {

        Function<ResultSet, Order> mapperFunction = (resultSet) -> {
            try {
                Order order = new Order();
                order.setCode(resultSet.getString(1));
                return order;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        };

        List<Object> resultList = new ArrayList<>();

        Connection connection = ConnectionManager.getInstance().getConnection(databaseConfig.getNode().get(0));

        String procStr = "{call test(?)}";
        CallableStatement callableStatement = connection.prepareCall(procStr);
        callableStatement.setString(1, "霍姆");
        callableStatement.execute();
        ResultSet resultSet = callableStatement.getResultSet();
        while (resultSet.next()) {
            resultList.add(mapperFunction.apply(resultSet));
        }

        resultList.forEach(item -> {
            System.out.println(item.getClass());
        });

        resultSet.close();
        callableStatement.close();
        ConnectionManager.getInstance().closeConnection(databaseConfig.getNode().get(0).getNodeName(), connection);
    }

    @Test
    public void test13() throws Exception {
        ProcessQuery<Order> processQuery = this.session.createProcessQuery("{call test(?)}");
        processQuery.setParameters(new String[]{"蚂蚁"});
        Function<ResultSet, Order> mapperFunction = (resultSet) -> {
            try {
                Order order = new Order();
                order.setCode(resultSet.getString(1));
                return order;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        };
        Order order = processQuery.unique(mapperFunction);
        System.out.println(order.getCode());
    }

    @Test
    public void test14() throws Exception {
        Criteria criteria = session.creatCriteria(Order.class);
        criteria.add(Restrictions.equ("uuid", 1011011L));
        criteria.delete();
    }

    @Test
    public void test15() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(5000);
        for (int index = 0; index < 5000; index++) {
            Thread thread = new Thread(() -> {
                try {
                    countDownLatch.await();
                    Order order = (Order) session.findOne(Order.class, 1);
                    System.out.println(order.getCode() + "=======================");
                } catch (InterruptedException e) {
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
