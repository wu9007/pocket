package org.homo.pocket.session;

import org.homo.pocket.model.BaseEntity;
import org.homo.pocket.criteria.Criteria;
import org.homo.pocket.query.AbstractQuery;

/**
 * @author wujianchuan 2018/12/31
 */

public interface Session {

    /**
     * 开启Session，拿到数据库链接并开启
     */
    void open();

    /**
     * 关闭数据库链接
     */
    void close();

    /**
     * 获取事务对象
     *
     * @return 事务对象
     */
    Transaction getTransaction();

    /**
     * 获取SQL查询对象
     *
     * @param sql 查询语句
     * @return 查询对象
     */
    AbstractQuery createSQLQuery(String sql);

    /**
     * 获取SQL规范对象
     *
     * @param clazz 实体类类型
     * @return SQL规范对象
     */
    Criteria creatCriteria(Class clazz);

    /**
     * 保存
     *
     * @param entity 实体对象
     * @return 影响行数
     * @throws Exception 异常
     */
    int save(BaseEntity entity) throws Exception;

    /**
     * 更新实体
     *
     * @param entity 实体对象
     * @return 影响行数
     * @throws Exception 异常
     */
    int update(BaseEntity entity) throws Exception;

    /**
     * 删除实体
     *
     * @param entity 实体对象
     * @return 影响行数
     * @throws Exception 异常
     */
    int delete(BaseEntity entity) throws Exception;

    /**
     * 查询对象
     *
     * @param clazz 类类型
     * @param uuid  数据标识
     * @return 实体对象
     * @throws Exception 异常
     */
    Object findOne(Class clazz, Long uuid) throws Exception;

    /**
     * 强制通过数据库查询数据
     *
     * @param clazz 类类型
     * @param uuid  数据标识
     * @return 实体对象
     * @throws Exception 异常
     */
    Object findDirect(Class clazz, Long uuid) throws Exception;

    /**
     * 获取最大数据标识
     *
     * @param clazz 实体类型
     * @return 最大值
     * @throws Exception 语句异常
     */
    long getMaxUuid(Class clazz) throws Exception;

    /**
     * 删除该数据的缓存
     *
     * @param entity 实体对象
     */
    void removeCache(BaseEntity entity);
}
