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
public class LogicCacheUtils implements CacheUtils {

    public static final String NAME = "logicCacheUtils";
    private final RedisTemplate<String, Object> logicRedisTemplate;
    private final StringRedisTemplate logicStringRedisTemplate;

    private final Map<String, String> mapLock = new ConcurrentHashMap<>(100);

    public LogicCacheUtils(StringRedisTemplate logicStringRedisTemplate, RedisTemplate<String, Object> logicRedisTemplate) {

        this.logicRedisTemplate = logicRedisTemplate;

        this.logicStringRedisTemplate = logicStringRedisTemplate;
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
        this.logicStringRedisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void set(String key, Object value) {
        this.logicRedisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void set(String key, Object value, Long timeOut) {
        this.logicRedisTemplate.opsForValue().set(key, value, timeOut, TimeUnit.SECONDS);
    }

    @Override
    public String getValue(String key) {
        return logicStringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public Object getObj(String key) {
        return this.logicRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        this.logicRedisTemplate.delete(key);
    }

    @Override
    public boolean exists(String key) {
        Boolean exists = this.logicRedisTemplate.hasKey(key);
        return exists != null && exists;
    }
}
