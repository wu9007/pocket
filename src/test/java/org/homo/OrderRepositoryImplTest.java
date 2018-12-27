package org.homo;

import org.homo.authority.model.User;
import org.homo.common.constant.OperateTypes;
import org.homo.demo.model.Order;
import org.homo.common.repository.AbstractRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

/**
 * @author wujianchuan 2018/12/26
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class OrderRepositoryImplTest {
    @Autowired
    AbstractRepository<Order> repository;

    @Test
    public void test1() {
        Order order = Order.newInstance("ABC-001", new BigDecimal("12.593"));
        order.setUuid("sl92mm34j4mndkj4nmd");
        User user = User.newInstance("Home", "霍姆");
        int effect = repository.getProxy().update(order, user);
        System.out.println("影响行数：" + effect);
    }

    @Test
    public void test2() {
        System.out.println(OperateTypes.SAVE);
    }
}
