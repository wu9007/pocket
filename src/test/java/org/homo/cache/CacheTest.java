package org.homo.cache;

import org.homo.Application;
import org.homo.core.model.BaseEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

/**
 * @author wujianchuan 2018/12/26
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class CacheTest {

    @Before
    public void setup() {
    }

    @Test
    public void test1() throws ExecutionException {
        for (int index = 1; index < 10; index <<= 1) {
            System.out.println(CacheManager.get("A-001").getDescribe());
            CacheManager.refresh("A-001");
        }
    }
}
