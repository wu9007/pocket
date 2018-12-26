package homo;

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
        repository.getProxy().save();
    }
}
