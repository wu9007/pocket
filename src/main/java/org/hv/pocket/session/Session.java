package org.hv.pocket.session;

import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.session.actions.BuildDictionary;
import org.hv.pocket.session.actions.BuildTransaction;
import org.hv.pocket.session.actions.OperateDictionary;

import java.sql.Connection;

/**
 * @author wujianchuan 2018/12/31
 */

public interface Session extends BuildDictionary, OperateDictionary, BuildTransaction {

    /**
     * 获取缓存持有者
     *
     * @return cache holder.
     */
    CacheHolder getCacheHolder();

    /**
     * 获取session name
     *
     * @return session name
     */
    String getSessionName();

    /**
     * 获取数据库配置节点
     *
     * @return database node config
     */
    DatabaseNodeConfig getDatabaseNodeConfig();

    /**
     * 获取数据库连接
     *
     * @return connection
     */
    Connection getConnection();
}
