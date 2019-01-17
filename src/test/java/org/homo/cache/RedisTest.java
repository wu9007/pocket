package org.homo.cache;

import org.homo.Application;
import org.homo.orderdemo.model.Order;
import org.homo.pocket.utils.CacheUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/17
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class RedisTest {

    private Logger logger = LoggerFactory.getLogger(RedisTest.class);

    @Autowired
    RedisTemplate<String, Object> mainRedisTemplate;
    @Autowired
    @Qualifier(value = "stringBenchRedisTemplate")
    StringRedisTemplate stringBenchRedisTemplate;
    @Autowired
    private CacheUtils cache;

    @Before
    public void setup() throws SQLException {
    }

    @After
    public void destroy() throws SQLException {
    }

    @Test
    public void test1() {
        mainRedisTemplate.opsForValue().set("session", "123456");
        stringBenchRedisTemplate.opsForValue().set("session", "654321");
        logger.info((String) mainRedisTemplate.opsForValue().get("session"));
        logger.info(stringBenchRedisTemplate.opsForValue().get("session"));
    }

    @Test
    public void test2() {
        cache.set("id", "001");
        logger.info(cache.getValue("id"));
    }

    @Test
    public void test3() {
        Order order = Order.newInstance("A-009", new BigDecimal("120.5"));
        cache.set("order", order);
        Order cacheOrder = (Order) cache.getObj("order");
        logger.info(String.valueOf(cacheOrder.getCode()));
        logger.info(String.valueOf(cache.exists("order")));
    }
}
