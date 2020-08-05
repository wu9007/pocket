package org.hv.pocket.lunch;

import org.hv.pocket.exception.MapperException;

/**
 * @author wujianchuan
 */
public interface PocketConfig {
    /**
     * 初始化持久化资源
     *
     * @throws MapperException e
     */
    void init() throws MapperException;
}
