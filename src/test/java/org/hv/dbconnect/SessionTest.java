package org.hv.dbconnect;

import org.hv.Application;
import org.hv.demo.model.Order;
import org.hv.demo.model.RelevantBill;
import org.hv.demo.model.RelevantBillDetail;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        order.setPrice(new BigDecimal("100.0"));
        this.session.save(order);
        this.session.delete(order);
    }
}
