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
public interface CacheUtils {

    Map<String, String> getMapLock();

    String generateKey(String sessionName, Class clazz, Serializable uuid);

    void set(String key, String value);

    void set(String key, Object value);

    void set(String key, Object value, Long timeOut);

    String getValue(String key);

    Object getObj(String key);

    void delete(String key);

    boolean exists(String key);
}
