package org.hv.pocket.lunch;

import org.hv.pocket.exception.PocketMapperException;

/**
 * @author wujianchuan
 */
public interface PocketConfig {
    /**
     * 初始化持久化资源
     *
     * @throws PocketMapperException e
     */
    void init() throws PocketMapperException;
}
