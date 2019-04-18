package org.hunter.pocket.cache;

import java.io.Serializable;
import java.util.Map;

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
