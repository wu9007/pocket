package org.homo.dbconnect.session;

import org.homo.core.model.BaseEntity;
import org.homo.dbconnect.transaction.Transaction;
import org.homo.dbconnect.query.AbstractQuery;

import java.sql.SQLException;

/**
 * @author wujianchuan 2018/12/31
 */

public interface Session {

    /**
     * 获取事务对象
     *
     * @return 事务对象
     */
    Transaction getTransaction();

    /**
     * 保存实体
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int save(BaseEntity entity) throws SQLException, IllegalAccessException;

    /**
     * 更新实体
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int update(BaseEntity entity);

    /**
     * 删除实体
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int delete(BaseEntity entity);

    /**
     * 查询实体
     *
     * @param clazz 对象类型
     * @param uuid 数据标识
     * @return 实体对象
     */
    BaseEntity findOne(Class clazz, Long uuid) throws SQLException, IllegalAccessException, InstantiationException;

    /**
     * 获取SQL查询对象
     *
     * @param sql 查询语句
     * @return 查询对象
     */
    AbstractQuery createSQLQuery(String sql);

    /**
     * 获取最大数据标识
     * @param clazz 实体类型
     * @return 最大值
     */
    long getMaxUuid(Class clazz) throws SQLException;
}
