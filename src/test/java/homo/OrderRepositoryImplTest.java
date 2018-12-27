package homo;

import homo.constant.OperateTypes;
import homo.demo.model.Order;
import homo.repository.AbstractRepository;
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
    AbstractRepository<Order> repository;

    @Test
    public void test1() {
        Order order = new Order();
        order.setUuid("837423981236");
        order.setCode("ABC-001");
        User user = User.newInstance("Home", "霍姆");
        int effect = repository.getProxy().update(order, user);
        System.out.println(effect);
    }

    @Test
    public void test2() {
        System.out.println(OperateTypes.SAVE);
    }
}
