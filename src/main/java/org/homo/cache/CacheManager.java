package org.homo.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.homo.authority.model.User;
import org.homo.core.model.BaseEntity;
import org.springframework.lang.NonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author wujianchuan 2018/12/31
 */
public class CacheManager {
    private final static LoadingCache<String, BaseEntity> ENTITY_CACHE
            = CacheBuilder.newBuilder()
            .concurrencyLevel(8)
            .expireAfterWrite(8, TimeUnit.SECONDS)
            .initialCapacity(10)
            .maximumSize(100)
            .recordStats()
            .removalListener((notification) -> {
                System.out.println(notification.getKey() + " was removed, cause is " + notification.getCause());
                System.out.println(notification.getKey() + " was removed, cause is " + notification.getCause());
            })
            .build(
                    new CacheLoader<String, BaseEntity>() {
                        @Override
                        public BaseEntity load(@NonNull String key) throws Exception {
                            System.out.println("数据库直查。" + key);
                            User user = User.newInstance("Home", "霍姆");
                            user.setUuid(1L);
                            return user;
                        }
                    }
            );

    private CacheManager() {
    }

    public static BaseEntity get(String key) throws ExecutionException {
        return ENTITY_CACHE.get(key);
    }

    public static void refresh(String key) {
        ENTITY_CACHE.refresh(key);
    }
}
