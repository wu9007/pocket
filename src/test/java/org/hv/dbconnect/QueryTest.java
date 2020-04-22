package org.hv.dbconnect;

import org.hv.Application;
import org.hv.demo.model.Order;
import org.hv.pocket.query.SQLQuery;
import org.hv.pocket.session.Session;
import org.hv.pocket.session.SessionFactory;
import org.hv.pocket.session.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author wujianchuan 2019/1/15
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class QueryTest {

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
    public void test1() throws SQLException, IllegalAccessException {
        Order order = new Order();
        order.setCode("F-00x");
        order.setType("001");
        order.setTime(LocalDateTime.now());
        order.setPrice(new BigDecimal("99.56789"));
        order.setDay(LocalDate.now());
        this.session.save(order);
        SQLQuery query = this.session.createSQLQuery("select uuid as uuid,code as code,'微信支付' as typeName from tbl_order where uuid = :uuid", Order.class);
        Order repositoryOrder = (Order) query.setParameter("uuid", order.getIdentify()).unique();
        System.out.println(repositoryOrder.getPrice());
        this.session.delete(repositoryOrder);
    }

    @Test
    public void test2() throws SQLException {
        SQLQuery query = this.session.createSQLQuery("select uuid uuid,code code,price price from tbl_order", Order.class);
        List<Order> orders = query.list();
        orders.forEach(order -> System.out.println(order.getCode()));
    }

    @Test
    public void test3() throws SQLException {
        SQLQuery query = this.session.createSQLQuery("select uuid,code,price from tbl_order", Order.class);
        List<Order> orders = query.limit(0, 5).list();
        orders.forEach(order -> System.out.println(order.getPrice()));
    }

    @Test
    public void test4() throws SQLException {
        SQLQuery query = this.session.createSQLQuery("select uuid, code from tbl_order where CODE = :ORDER_CODE AND DAY < :DAY")
                .mapperColumn("label", "value")
                .setParameter("ORDER_CODE", "C-001")
                .setParameter("DAY", new Date());
        List<Map<String, String>> orders = query.list();
        System.out.println(orders.size());
    }

    @Test
    public void test5() throws SQLException {
        List<String> types = Arrays.asList("006", "007", "008", "009");
        SQLQuery query = this.session.createSQLQuery("select uuid, code from tbl_order where TYPE IN(:TYPE)")
                .mapperColumn("label", "value")
                .setParameter("TYPE", types);
        List<Map<String, String>> orders = query.list();
        System.out.println(orders.size());
    }

    @Test
    public void test6() throws SQLException {
        SQLQuery query = this.session.createSQLQuery("select uuid from tbl_order where CODE = :ORDER_CODE AND DAY < :DAY")
                .setParameter("ORDER_CODE", "C-001")
                .setParameter("DAY", new Date());
        List<?> orders = query.list();
        System.out.println(orders.size());
    }
}
