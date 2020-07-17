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
     * @throws SQLException e
     */
    <T extends AbstractEntity> T findOne(Class<T> clazz, Serializable identify) throws SQLException;


    /**
     * 查询对象
     *
     * @param clazz    类类型
     * @param identify 数据标识
     * @param cascade  是否进行级联保存操作
     * @return 实体对象
     * @throws SQLException e
     */
    <T extends AbstractEntity> T findOne(Class<T> clazz, Serializable identify, boolean cascade) throws SQLException;

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
     * @throws SQLException 语句异常
     */
    <T extends AbstractEntity> T findDirect(Class<T> clazz, Serializable identify) throws SQLException;

    /**
     * 强制通过数据库查询数据
     *
     * @param clazz    类类型
     * @param identify 数据标识
     * @param cascade  是否进行级联保存操作
     * @return 实体对象
     * @throws SQLException 语句异常
     */
    <T extends AbstractEntity> T findDirect(Class<T> clazz, Serializable identify, boolean cascade) throws SQLException;

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
     * @throws SQLException           语句异常
     * @throws IllegalAccessException e
     */
    int save(AbstractEntity entity, boolean cascade) throws SQLException, IllegalAccessException;

    /**
     * 保存(NULL同样进行保存)
     *
     * @param entity 实体对象
     * @return 影响行数
     * @throws SQLException 语句异常
     */
    int forcibleSave(AbstractEntity entity) throws SQLException;

    /**
     * 保存(NULL同样进行保存)
     *
     * @param entity  实体对象
     * @param cascade 是否进行级联保存操作
     * @return 影响行数
     * @throws SQLException           语句异常
     * @throws IllegalAccessException e
     */
    int forcibleSave(AbstractEntity entity, boolean cascade) throws SQLException, IllegalAccessException;

    /**
     * 更新实体
     *
     * @param entity 实体对象
     * @return 影响行数
     * @throws SQLException 语句异常
     */
    int update(AbstractEntity entity) throws SQLException;


    /**
     * 更新实体
     *
     * @param entity  实体对象
     * @param cascade 是否进行级联更新操作
     * @return 影响行数
     * @throws SQLException           语句异常
     * @throws IllegalAccessException e
     */
    int update(AbstractEntity entity, boolean cascade) throws SQLException, IllegalAccessException;

    /**
     * 级联删除
     *
     * @param entity 实体对象
     * @return 影响行数
     * @throws SQLException           语句异常
     * @throws IllegalAccessException e
     */
    int delete(AbstractEntity entity) throws SQLException, IllegalAccessException;

    /**
     * 删除实体
     *
     * @param entity  实体对象
     * @param cascade 是否进行级联更新操作
     * @return 影响行数
     * @throws SQLException           语句异常
     * @throws IllegalAccessException e
     */
    int delete(AbstractEntity entity, boolean cascade) throws SQLException, IllegalAccessException;
}
