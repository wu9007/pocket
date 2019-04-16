package org.hunter.pocket.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author wujianchuan 2019/1/17
 */
@Component
public class BaseCacheUtils implements CacheUtils {

    public static final String NAME = "baseCacheUtils";
    private final RedisTemplate<String, Object> baseRedisTemplate;
    private final StringRedisTemplate baseStringRedisTemplate;

    private final Map<String, String> mapLock = new ConcurrentHashMap<>(100);

    public BaseCacheUtils(StringRedisTemplate baseStringRedisTemplate, RedisTemplate<String, Object> baseRedisTemplate) {

        this.baseRedisTemplate = baseRedisTemplate;

        this.baseStringRedisTemplate = baseStringRedisTemplate;
    }

    @Override
    public Map<String, String> getMapLock() {
        return this.mapLock;
    }

    @Override
    public String generateKey(String sessionName, Class clazz, Serializable uuid) {
        return sessionName + clazz.getName() + uuid;
    }

    @Override
    public void set(String key, String value) {
        this.baseStringRedisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void set(String key, Object value) {
        this.baseRedisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void set(String key, Object value, Long timeOut) {
        this.baseRedisTemplate.opsForValue().set(key, value, timeOut, TimeUnit.SECONDS);
    }

    @Override
    public String getValue(String key) {
        return baseStringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public Object getObj(String key) {
        return this.baseRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        this.baseRedisTemplate.delete(key);
    }

    @Override
    public boolean exists(String key) {
        Boolean exists = this.baseRedisTemplate.hasKey(key);
        return exists != null && exists;
    }
}
