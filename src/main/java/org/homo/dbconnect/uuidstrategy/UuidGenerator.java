package org.homo.dbconnect.uuidstrategy;

import org.homo.dbconnect.session.Session;

import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/2
 */
public interface UuidGenerator {

    /**
     * 获取数据标识
     *
     * @param clazz   实体类型
     * @param session 缓存管理类
     * @return 数据标识
     * @throws SQLException sql语句异常
     */
    long getUuid(Class clazz, Session session) throws SQLException;
}
