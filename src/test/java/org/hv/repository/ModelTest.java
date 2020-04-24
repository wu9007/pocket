package org.hv.repository;

import org.hv.Application;
import org.hv.demo.model.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ModelTest {
    @Before
    public void setup() {
    }

    @After
    public void destroy() {
    }

    @Test
    public void test1() {
        Order order = new Order();
        order.putIdentify("001");
        order.setCode("002");
        order.setPrice(new BigDecimal("11.2"));
        order.setDay(LocalDate.now());
        order.setState(false);
        order.setSort(2);

        Order order1 = new Order();
        order1.putIdentify("001");
        order1.setCode("002");
        order1.setPrice(new BigDecimal("11.2"));
        order1.setDay(order.getDay());
        order1.setState(false);
        order1.setSort(1);
        System.out.println(order.equals(order1));
    }
}
