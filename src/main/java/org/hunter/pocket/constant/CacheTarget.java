package org.hunter.pocket.constant;

import org.hunter.pocket.cache.BaseCacheUtils;
import org.hunter.pocket.cache.LogicCacheUtils;

/**
 * @author wujianchuan
 */
public enum CacheTarget {

    /**
     * 数据库层缓存
     */
    DATA_BASE(BaseCacheUtils.NAME),
    /**
     * 业务层缓存
     */
    BUSINESS(LogicCacheUtils.NAME);

    private String cacheUtilsName;

    CacheTarget(String cacheUtilsName) {
        this.cacheUtilsName = cacheUtilsName;
    }

    public String getCacheUtilsName() {
        return cacheUtilsName;
    }}
