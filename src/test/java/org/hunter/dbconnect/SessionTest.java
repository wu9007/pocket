package org.hunter.dbconnect;

import org.hunter.Application;
import org.hunter.PocketExecutor;
import org.hunter.demo.model.Commodity;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @author wujianchuan 2019/1/15
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SessionTest {

    private Session session;
    private Transaction transaction;
    private List<Commodity> commodities = new ArrayList<>();

    @Before
    public void setup() {
        IntStream.range(1, 10).forEach((index) -> {
            Commodity commodity = new Commodity();
            commodity.setName("c1");
            commodity.setType("001");
            commodity.setPrice(new BigDecimal("11.2"));
            commodities.add(commodity);
        });
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
        order.setTime(new Date());
        order.setPrice(new BigDecimal("99.56789"));
        order.setDay(new Date());
        this.session.saveNotNull(order);
        this.session.save(order);
        session.delete(order);
    }

    @Test
    public void test2() throws SQLException {
        Order order = (Order) session.findOne(Order.class, 10110194);
        order.setState(null);
        order.setPrice(new BigDecimal("120.96"));
        order.setType("001");
        session.update(order);
    }

    @Test
    public void test3() throws Exception {
        long uuid = session.getMaxUuid(101, Order.class);
        System.out.println(uuid);
    }

    @Test
    public void test4() throws Exception {
        Order order = new Order();
        order.setState(null);
        order.setPrice(new BigDecimal("120.96"));
        order.setType("001");
        order.setCommodities(commodities);
        System.out.println(session.save(order, true));
    }

    @Test
    public void test5() throws Exception {
        Order order = new Order();
        order.setState(null);
        order.setPrice(new BigDecimal("120.96"));
        order.setType("001");
        order.setCommodities(commodities);
        System.out.println(session.save(order));
        commodities.parallelStream().forEach(commodity1 -> {
            try {
                commodity1.setOrder(order.getUuid());
                session.save(commodity1);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void test6() {
        Order order = new Order();
        order.setState(null);
        order.setPrice(new BigDecimal("120.96"));
        order.setType("001");
        order.setCommodities(commodities);

        Commodity commodity = new Commodity();
        commodity.setName("c1");
        commodity.setType("001");
        commodity.setPrice(new BigDecimal("11.2"));

        IntStream.range(1, 10).parallel().forEach((i) -> {
            try {
                PocketExecutor.execute(Executors.newFixedThreadPool(10), 10, () -> {
                    try {
                        session.save(order, true);
                        session.save(commodity, true);
                    } catch (SQLException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void test7() throws SQLException, IllegalAccessException {
        Order order = new Order();
        order.setState(null);
        order.setPrice(new BigDecimal("120.96"));
        order.setType("001");
        order.setCommodities(commodities);

        session.save(order, true);

        order = (Order) session.findOne(Order.class, order.getUuid());
        List<Commodity> details = order.getCommodities();
        details.remove(0);
        details.get(0).setType("00000");

        Commodity commodity = new Commodity();
        commodity.setName("C001");
        commodity.setType("002");
        commodity.setPrice(new BigDecimal("868"));

        details.add(commodity);
        session.update(order, true);
        System.out.println(session.delete(order));
    }
}
