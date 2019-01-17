package org.homo.cache;

import org.homo.Application;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/17
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class RedisTest {

    Logger logger = LoggerFactory.getLogger(RedisTest.class);

    @Autowired
    RedisTemplate<String, String> mainRedisTemplate;
    @Autowired
    RedisTemplate<String, String> benchRedisTemplate;

    @Before
    public void setup() throws SQLException {
    }

    @After
    public void destroy() throws SQLException {
    }

    @Test
    public void test1() {
        mainRedisTemplate.opsForValue().set("session", "123456");
        benchRedisTemplate.opsForValue().set("session", "654321");
        logger.info(mainRedisTemplate.opsForValue().get("session"));
        logger.info(benchRedisTemplate.opsForValue().get("session"));
    }
}
