package org.hv.pocket.cache;

/**
 * @author wujianchuan
 */
public interface Cache<K, V> {

    /**
     * put into cache pool
     *
     * @param key   key
     * @param value value
     */
    void put(K key, V value);

    /**
     * get cache content
     *
     * @param key key
     * @return value
     */
    V get(K key);

    /**
     * drop cache element
     *
     * @param key key
     */
    void remove(K key);

    /**
     * clear the cache pool
     */
    void clear();
}
