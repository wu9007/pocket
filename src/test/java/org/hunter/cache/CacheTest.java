package org.hunter.cache;

import org.hunter.Application;
import org.hunter.PocketExecutor;
import org.hunter.demo.model.Order;
import org.hunter.demo.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.Executors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class CacheTest {

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void test1() throws InterruptedException {
        PocketExecutor.execute(Executors.newFixedThreadPool(1000), 1000, () -> {
            List<Order> orders = orderRepository.loadByCode("C-001");
            orders.forEach(order -> System.out.println(order.getPrice()));
        });
    }
}
