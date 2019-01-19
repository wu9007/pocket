package org.hunter.pocket.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author wujianchuan 2019/1/17
 */
@Component
public class CacheUtils {

    private RedisTemplate<String, Object> baseRedisTemplate;
    private StringRedisTemplate baseStringRedisTemplate;

    public Map<String, String> mapLock = new ConcurrentHashMap<>(100);

    public CacheUtils(StringRedisTemplate baseStringRedisTemplate, RedisTemplate<String, Object> baseRedisTemplate) {

        this.baseRedisTemplate = baseRedisTemplate;

        this.baseStringRedisTemplate = baseStringRedisTemplate;
    }

    public String generateKey(String sessionName, Class clazz, Long uuid) {
        return sessionName + clazz.getName() + uuid;
    }

    public void set(String key, String value) {
        this.baseStringRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value) {
        this.baseRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, Long timeOut) {
        this.baseRedisTemplate.opsForValue().set(key, value, timeOut, TimeUnit.SECONDS);
    }

    public String getValue(String key) {
        return baseStringRedisTemplate.opsForValue().get(key);
    }

    public Object getObj(String key) {
        return this.baseRedisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        this.baseRedisTemplate.delete(key);
    }

    public boolean exists(String key) {
        Boolean exists = this.baseRedisTemplate.hasKey(key);
        return exists != null && exists;
    }
}
