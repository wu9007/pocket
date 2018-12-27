package homo;

import homo.constant.OperateTypes;
import homo.demo.model.Order;
import homo.demo.repository.OrderRepositoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author wujianchuan 2018/12/26
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class OrderRepositoryImplTest {
    @Autowired
    OrderRepositoryImpl repository;

    @Test
    public void test1() {
        Order order = new Order();
        order.setUuid("837423981236");
        order.setCode("ABC-001");
        int effect = repository.getProxy().save(order);
        System.out.println(effect);
    }

    @Test
    public void test2() {
        System.out.println(OperateTypes.SAVE);
    }
}
