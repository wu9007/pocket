package org.hv.pocket.session;

import org.hv.pocket.cache.Cache;
import org.hv.pocket.cache.LruCache;
import org.hv.pocket.model.AbstractEntity;

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
        if (value != null) {
            this.lruCache.put(key, ((AbstractEntity) value).clone());
        } else {
            this.lruCache.put(key, null);
        }
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
