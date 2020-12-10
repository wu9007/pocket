package org.hv.dbconnect;

import org.hv.Application;
import org.hv.demo.model.Order;
import org.hv.demo.model.OrderView;
import org.hv.pocket.constant.EncryptType;
import org.hv.pocket.query.SQLQuery;
import org.hv.pocket.session.Session;
import org.hv.pocket.session.SessionFactory;
import org.hv.pocket.session.Transaction;
import org.hv.pocket.utils.EncryptUtil;
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
        int n = this.session.save(order);
        System.out.println("保存条数：" + n);
        SQLQuery query = this.session.createSQLQuery("select uuid as uuid,code as code,'微信支付' as typeName from tbl_order where uuid = :uuid", Order.class);
        Order repositoryOrder = (Order) query.setParameter("uuid", order.loadIdentify()).unique();
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
        List<String> orders = query.list();
        System.out.println(orders.size());
    }

    @Test
    public void test7() throws SQLException {
        SQLQuery query = this.session.createSQLQuery("select CODE as code, PRICE as price, TIME as time from tbl_order where CODE = :ORDER_CODE", OrderView.class)
                .setParameter("ORDER_CODE", EncryptUtil.encrypt(EncryptType.DES, "sward18713839007", "C-006"));
        List<OrderView> orders = query.list();
        System.out.println(orders.size());
    }

    @Test
    public void test8() throws SQLException {
        SQLQuery query = this.session.createSQLQuery("delete from tbl_order where CODE = :ORDER_CODE AND DAY < :DAY", OrderView.class)
                .setParameter("ORDER_CODE", "C-001")
                .setParameter("DAY", new Date());
        int row = query.execute();
        System.out.println(row);
    }

    @Test
    public void test9() throws SQLException {
        SQLQuery queryInsert = this.session.createSQLQuery("insert into tbl_order(uuid,code,price) values('00001','C-00001', 10)");
        int rowInsert = queryInsert.execute();
        System.out.println(rowInsert);
        SQLQuery queryUpdate = this.session.createSQLQuery("update tbl_order set price = 11 where uuid='00001'");
        queryUpdate.execute();
        int rowUpdate = queryUpdate.execute();
        System.out.println(rowUpdate);
        SQLQuery queryDelete = this.session.createSQLQuery("delete from tbl_order where uuid='00001'");
        int rowDeleted = queryDelete.execute();
        System.out.println(rowDeleted);
    }

    @Test
    public void test10() throws SQLException {
        SQLQuery queryInsert = this.session.createSQLQuery("insert into tbl_order(uuid,code,price) values(:ID, :IDCODE, :PRICE)");
        for (int index = 0; index < 10; index++) {
            queryInsert.setParameter("ID", "2220" + index)
                    .setParameter("IDCODE", "C-00" + index)
                    .setParameter("PRICE", index)
                    .addBatch();
        }
        int[] rowInserts = queryInsert.executeBatch();
        System.out.println(Arrays.toString(rowInserts));
        SQLQuery queryDelete = this.session.createSQLQuery("delete from tbl_order where uuid = :UUID");
        for (int index = 0; index < 10; index++) {
            queryDelete.setParameter("UUID", "2220" + index)
                    .addBatch();
        }
        int[] rowDeletes = queryDelete.executeBatch();
        System.out.println(Arrays.toString(rowDeletes));
    }

    @Test
    public void test11() throws SQLException {
        SQLQuery query = this.session.createSQLQuery();
        LocalDateTime localDateTime = query.now();
        System.out.println(localDateTime.toString());
    }

    @Test
    public void test12() throws SQLException {
        SQLQuery query = this.session.createSQLQuery("SELECT UUID FROM T_HISTORY ORDER BY UUID")
                .limit(0, 10);
        List<String> uuidList = query.list();
        uuidList.forEach(System.out::println);
    }

    @Test
    public void test13() throws SQLException {
        SQLQuery query = this.session.createSQLQuery("select uuid uuid,code code,price price from tbl_order limit 0, 1", Order.class);
        Order order = (Order) query.unique();
        System.out.println(order.getCode());
    }
}
