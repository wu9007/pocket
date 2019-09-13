package org.hunter.pocket.session;

import org.hunter.pocket.cache.Cache;
import org.hunter.pocket.cache.LruCache;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wujianchuan 2019/1/17
 */
public class CacheHolder {
    private Cache<String, Object> lruCache;
    private ConcurrentHashMap<String, String> mapLock = new ConcurrentHashMap<>(100);

    CacheHolder(int cacheSize) {
        this.lruCache = new LruCache<>(cacheSize);
    }

    ConcurrentHashMap<String, String> getMapLock() {
        return mapLock;
    }

    public String generateKey(Class clazz, Serializable uuid) {
        return clazz.getName() + uuid;
    }

    void set(String key, Object value) {
        this.lruCache.put(key, value);
    }

    public void remove(String key) {
        this.lruCache.remove(key);
    }

    public void clear() {
        this.lruCache.clear();
    }

    Object get(String key) {
        return this.lruCache.get(key);
    }
}
