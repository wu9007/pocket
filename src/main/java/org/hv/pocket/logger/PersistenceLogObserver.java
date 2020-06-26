package org.hv.pocket.logger;

/**
 * @author wujianchuan
 */
public interface PersistenceLogObserver {

    /**
     * 处理持久化日志
     *
     * @param persistenceLog log
     */
    void dealWithPersistenceLog(String persistenceLog);
}
