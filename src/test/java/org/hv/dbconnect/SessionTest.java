package org.hv.dbconnect;

import org.hv.Application;
import org.hv.demo.model.History;
import org.hv.demo.model.Order;
import org.hv.demo.model.OrderType;
import org.hv.demo.model.RelevantBill;
import org.hv.demo.model.RelevantBillDetail;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author wujianchuan 2019/1/15
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SessionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionTest.class);
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

        RelevantBill newOrder = new RelevantBill();
        newOrder.setCode("C-0001");
        newOrder.setAvailable(true);
        newOrder.putIdentify("101315");
        List<RelevantBillDetail> details = IntStream.range(0, 10).mapToObj(index -> {
            RelevantBillDetail detail = new RelevantBillDetail();
            detail.setName("明细" + index);
            detail.setPrice(new BigDecimal("100.0"));
            return detail;
        }).collect(Collectors.toList());
        newOrder.setDetails(details);
        int n = this.session.save(newOrder, true);
        System.out.println("保存条数：" + n);
        newOrder.getDetails().get(0).setName("==========");
        newOrder.setAvailable(false);
        n = this.session.update(newOrder, true);
        System.out.println("更新条数：" + n);
        RelevantBill repositoryOrder = this.session.findOne(RelevantBill.class, "101315");
        System.out.println(repositoryOrder.getCode());
        repositoryOrder.setCode("Hello-001");
        this.session.update(repositoryOrder);
        System.out.println(repositoryOrder.getCode());
        n = this.session.delete(repositoryOrder);
        System.out.println("删除条数：" + n);
    }

    @Test
    public void test2() throws SQLException, IllegalAccessException {
        Order order = new Order();
        order.setCode("ORDER-0001");
        order.setDay(LocalDate.now());
        order.setTime(LocalDateTime.now());
        order.setPrice(new BigDecimal("100.0"));
        session.save(order);
        session.delete(order);
    }

    @Test
    public void test3() throws InterruptedException {
        PocketExecutor.execute(Executors.newFixedThreadPool(10), 10, () -> {
            Order order = new Order();
            order.setCode("ORDER-F001");
            order.setDay(LocalDate.now());
            order.setPrice(new BigDecimal("200.0"));
            try {
                this.session.save(order);
            } catch (SQLException e) {
                LOGGER.debug(e.getMessage());
            }
        });
    }

    @Test
    public void test4() throws SQLException {
        for (int i = 0; i < 10; i++) {
            Order order = new Order();
            order.setCode("ORDER-F001");
            order.setDay(LocalDate.now());
            order.setPrice(new BigDecimal("200.0"));
            this.session.save(order);
        }
    }

    @Test
    public void test5() throws SQLException, IllegalAccessException {
        RelevantBill newOrder = new RelevantBill();
        newOrder.setCode("C-0001");
        newOrder.setAvailable(true);
        List<RelevantBillDetail> details = IntStream.range(0, 10).mapToObj(index -> {
            RelevantBillDetail detail = new RelevantBillDetail();
            detail.setName("明细" + index);
            detail.setPrice(new BigDecimal("100.0"));
            return detail;
        }).collect(Collectors.toList());
        newOrder.setDetails(details);
        int n = this.session.save(newOrder, true);
        System.out.println("保存条数：" + n);
        newOrder.getDetails().get(0).setName("==========");
        newOrder.setAvailable(false);
        n = this.session.update(newOrder, true);
        System.out.println("更新条数：" + n);
        RelevantBill repositoryOrder = this.session.findOne(RelevantBill.class, newOrder.loadIdentify());
        System.out.println(repositoryOrder.getCode());
        repositoryOrder.setCode("Hello-001");
        this.session.update(repositoryOrder);
        System.out.println(repositoryOrder.getCode());
        n = this.session.delete(repositoryOrder);
        System.out.println("删除条数：" + n);
    }

    @Test
    public void test6() throws SQLException, IllegalAccessException {
        RelevantBill newOrder = new RelevantBill();
        newOrder.setCode("C-1001");
        newOrder.setAvailable(true);

        int n = this.session.save(newOrder, true);
        System.out.println("保存条数：" + n);
        newOrder.setCode("C-1010");
        n = this.session.update(newOrder);
        System.out.println("更新条数：" + n);
        RelevantBill persistenceBill = this.session.findDirect(RelevantBill.class, newOrder.loadIdentify());
        System.out.println(persistenceBill.getCode());
        List<RelevantBill> relevantBills = this.session.list(RelevantBill.class);
        System.out.println(relevantBills.size());
    }

    @Test
    public void test7() throws SQLException {
        Session sessionUser = SessionFactory.getSession("user");
        Session sessionOrder = SessionFactory.getSession("order");
        sessionUser.open();
        sessionOrder.open();
        Transaction transactionUser = sessionUser.getTransaction();
        Transaction transactionOrder = sessionOrder.getTransaction();
        transactionUser.begin();
        transactionOrder.begin();

        History history = new History("test", LocalDate.now(), "测试", "001", "{\"userName\":\"测试\"}");
        sessionUser.save(history);
        transactionUser.commit();
        history.setUuid(null);
        sessionOrder.save(history);
        transactionOrder.commit();
        sessionOrder.close();
        sessionUser.close();
    }

    @Test
    public void test8() throws SQLException {
        OrderType orderType = new OrderType();
        orderType.setName("测试类型");
        this.session.save(orderType);
        orderType.setName("测试类型2");
        int row = this.session.update(orderType);
        System.out.println(row);
    }
}
