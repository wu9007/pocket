package org.hv.pocket.session.actions;

import org.hv.pocket.model.AbstractEntity;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

/**
 * @author wujianchuan
 */
public interface OperateDictionary {

    /**
     * 是否已关闭
     *
     * @return boolean
     */
    boolean getClosed();

    /**
     * 开启Session，拿到数据库链接并开启
     */
    void open();

    /**
     * 关闭数据库链接
     */
    void close();


    /**
     * 级联查询对象
     *
     * @param clazz    类类型
     * @param identify 数据标识
     * @return 实体对象
     */
    <T extends AbstractEntity> T findOne(Class<T> clazz, Serializable identify);


    /**
     * 查询对象
     *
     * @param clazz    类类型
     * @param identify 数据标识
     * @param cascade  是否进行级联保存操作
     * @return 实体对象
     */
    <T extends AbstractEntity> T findOne(Class<T> clazz, Serializable identify, boolean cascade);

    /**
     * 查询所有
     *
     * @param clazz 类类型
     * @return 所有实体对象
     */
    <E extends AbstractEntity> List<E> list(Class<E> clazz);

    /**
     * 查询所有
     *
     * @param clazz   类类型
     * @param cascade 是否级联
     * @return 所有实体对象
     */
    <E extends AbstractEntity> List<E> list(Class<E> clazz, boolean cascade);

    /**
     * 强制通过数据库级联查询数据
     *
     * @param clazz    类类型
     * @param identify 数据标识
     * @return 实体对象
     */
    <T extends AbstractEntity> T findDirect(Class<T> clazz, Serializable identify);

    /**
     * 强制通过数据库查询数据
     *
     * @param clazz    类类型
     * @param identify 数据标识
     * @param cascade  是否进行级联保存操作
     * @return 实体对象
     */
    <T extends AbstractEntity> T findDirect(Class<T> clazz, Serializable identify, boolean cascade);

    /**
     * 保存(NULL不纳入保存范围保留数据库默认值)
     *
     * @param entity 实体对象
     * @return 影响行数
     * @throws SQLException 语句异常
     */
    int save(AbstractEntity entity) throws SQLException;

    /**
     * 保存(NULL不纳入保存范围保留数据库默认值)
     *
     * @param entity  entity
     * @param cascade 是否进行级联保存操作
     * @return 影响行数（主+从）
     */
    int save(AbstractEntity entity, boolean cascade);

    /**
     * 保存(NULL同样进行保存)
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int forcibleSave(AbstractEntity entity);

    /**
     * 保存(NULL同样进行保存)
     *
     * @param entity  实体对象
     * @param cascade 是否进行级联保存操作
     * @return 影响行数
     */
    int forcibleSave(AbstractEntity entity, boolean cascade);

    /**
     * 更新实体
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int update(AbstractEntity entity);


    /**
     * 更新实体
     *
     * @param entity  实体对象
     * @param cascade 是否进行级联更新操作
     * @return 影响行数
     */
    int update(AbstractEntity entity, boolean cascade);

    /**
     * 根据数据标识删除(非级联)
     *
     * @param clazz    类类型
     * @param identify 数据标识
     * @param <T>      泛型
     * @return 影响行数
     */
    <T extends AbstractEntity> int deleteOne(Class<T> clazz, Serializable identify);

    /**
     * 级联删除
     *
     * @param entity 实体对象
     * @return 影响行数
     */
    int delete(AbstractEntity entity);

    /**
     * 删除实体
     *
     * @param entity  实体对象
     * @param cascade 是否进行级联更新操作
     * @return 影响行数
     */
    int delete(AbstractEntity entity, boolean cascade);
}
