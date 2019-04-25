package org.hunter.repository;

import org.hunter.Application;
import org.hunter.demo.model.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
        order.setUuid(null);
        order.setCode("002");
        Order order1 = new Order();
        order1.setUuid(null);
        order1.setCode("002");
        System.out.println(order.equals(order1));
    }
}
