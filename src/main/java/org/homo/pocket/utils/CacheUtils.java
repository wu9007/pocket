package org.homo.pocket.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author wujianchuan 2019/1/17
 */
@Component
public class CacheUtils {

    private RedisTemplate<String, Object> mainRedisTemplate;
    private StringRedisTemplate mainStringRedisTemplate;

    public CacheUtils(StringRedisTemplate mainStringRedisTemplate, RedisTemplate<String, Object> mainRedisTemplate) {
        this.mainStringRedisTemplate = mainStringRedisTemplate;
        this.mainRedisTemplate = mainRedisTemplate;
    }

    public void set(String key, String value) {
        this.mainStringRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value) {
        this.mainRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, Long timeOut) {
        this.mainRedisTemplate.opsForValue().set(key, value, timeOut, TimeUnit.SECONDS);
    }

    public String getValue(String key) {
        return mainStringRedisTemplate.opsForValue().get(key);
    }

    public Object getObj(String key) {
        return this.mainRedisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        this.mainRedisTemplate.delete(key);
    }

    public boolean exists(String key) {
        Boolean exists = this.mainRedisTemplate.hasKey(key);
        return exists != null && exists;
    }
}
