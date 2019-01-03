package org.homo.cache;

import org.homo.Application;
import org.homo.authority.model.User;
import org.homo.core.model.BaseEntity;
import org.homo.dbconnect.session.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

/**
 * @author wujianchuan 2018/12/26
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class CacheTest {

    @Autowired
    SessionFactory sessionFactory;

    @Before
    public void setup() {
    }

    @Test
    public void test1() throws ExecutionException {
        CacheManager cacheManager = CacheManager.getInstance();
        for (int index = 1; index < 10; index <<= 1) {
            System.out.println(cacheManager.get(sessionFactory.getSession("sessionName-demo"), User.class.getName() + "_1").getDescribe());
            cacheManager.refresh(User.class.getName() + "_1");
        }
    }
}
