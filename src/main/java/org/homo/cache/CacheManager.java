package org.homo.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.homo.core.model.BaseEntity;
import org.homo.dbconnect.session.Session;
import org.springframework.lang.NonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author wujianchuan 2018/12/31
 */
public class CacheManager {
    private static final CacheManager INSTANCE = new CacheManager();
    private final LoadingCache<String, BaseEntity> ENTITY_CACHE
            = CacheBuilder.newBuilder()
            .concurrencyLevel(8)
            .expireAfterWrite(8, TimeUnit.SECONDS)
            .initialCapacity(10)
            .maximumSize(100)
            .recordStats()
            .removalListener((notification) -> {
                System.out.println(notification.getKey() + " was removed, cause is " + notification.getCause());
            })
            .build(
                    new CacheLoader<String, BaseEntity>() {
                        @Override
                        public BaseEntity load(@NonNull String key) throws Exception {
                            String className = key.substring(0, key.indexOf("_"));
                            String uuid = key.substring(key.indexOf("_") + 1);
                            System.out.println("数据库直查。" + key);
                            return CacheManager.getInstance().session.findOne(Class.forName(className), Long.valueOf(uuid));
                        }
                    }
            );
    private Session session;

    private CacheManager() {
    }

    public static CacheManager getInstance() {
        return INSTANCE;
    }

    public BaseEntity get(Session session, String key) throws ExecutionException {
        this.session = session;
        return this.ENTITY_CACHE.get(key);
    }

    public void refresh(String key) {
        this.ENTITY_CACHE.refresh(key);
    }
}
