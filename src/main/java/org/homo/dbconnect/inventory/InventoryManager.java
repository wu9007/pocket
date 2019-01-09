package org.homo.dbconnect.inventory;

import org.homo.core.model.BaseEntity;
import org.homo.dbconnect.transaction.Transaction;
import org.homo.dbconnect.query.AbstractQuery;

/**
 * @author wujianchuan 2018/12/31
 */

public interface InventoryManager {

    /**
     * 对应的数据库名称
     *
     * @return 数据库名称
     */
    String getDbName();

    /**
     * 获取事务对象
     *
     * @return 事务对象
     */
    Transaction getTransaction();

    /**
     * 保存
     *
     * @param entity 实体对象
     * @return 实体对象
     * @throws Exception           异常
     */
    BaseEntity save(BaseEntity entity) throws Exception;

    /**
     * 更新实体
     *
     * @param entity 实体对象
     * @return 实体对象
     * @throws Exception           异常
     */
    BaseEntity update(BaseEntity entity) throws Exception;

    /**
     * 删除实体
     *
     * @param entity 实体对象
     * @return 影响行数
     * @throws Exception           异常
     */
    int delete(BaseEntity entity) throws Exception;

    /**
     * 查询对象
     *
     * @param clazz 类类型
     * @param uuid  数据标识
     * @return 实体对象
     * @throws Exception           异常
     */
    BaseEntity findOne(Class clazz, Long uuid) throws Exception;

    /**
     * 获取SQL查询对象
     *
     * @param sql 查询语句
     * @return 查询对象
     */
    AbstractQuery createSQLQuery(String sql);

    /**
     * 获取最大数据标识
     *
     * @param clazz 实体类型
     * @return 最大值
     * @throws Exception 语句异常
     */
    long getMaxUuid(Class clazz) throws Exception;
}
