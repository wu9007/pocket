package org.hunter.pocket.session;

import org.hunter.pocket.cache.Cache;
import org.hunter.pocket.cache.LruCache;
import org.hunter.pocket.config.DatabaseConfig;
import org.hunter.pocket.model.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author wujianchuan 2019/1/17
 */
@Component
public class EntityCache {
    private Cache<String, BaseEntity> lruCache;

    @Autowired
    public EntityCache(DatabaseConfig databaseConfig) {
        Integer cacheSize = databaseConfig.getCacheSize();
        if (cacheSize == null) {
            cacheSize = 1000;
        }
        this.lruCache = new LruCache<>(cacheSize);
    }

    public String generateKey(String sessionName, Serializable uuid) {
        return sessionName + uuid;
    }

    public <T extends BaseEntity> void set(String key, T value) {
        this.lruCache.put(key, value);
    }

    public BaseEntity get(String key) {
        return this.lruCache.get(key);
    }
}
