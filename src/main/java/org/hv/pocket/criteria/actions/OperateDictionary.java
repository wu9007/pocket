package org.hv.pocket.criteria.actions;

import org.hv.pocket.model.AbstractEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * @author wujianchuan
 */
public interface OperateDictionary {

    /**
     * 获取所有数据 (默认不进行级联查询)
     *
     * @return 对象集合
     */
    <E extends AbstractEntity> List<E> list();

    /**
     * 获取所有数据,持有条件集合可重复利用
     *
     * @return 对象集合
     */
    <E extends AbstractEntity> List<E> listNotCleanRestrictions();

    /**
     * 获取所有数据
     *
     * @param cascade 是否级联查询
     * @return 对象集合
     */
    <E extends AbstractEntity> List<E> list(boolean cascade);

    /**
     * 获取所有数据,持有条件集合可重复利用
     *
     * @param cascade 是否级联查询
     * @return 对象集合
     */
    <E extends AbstractEntity> List<E> listNotCleanRestrictions(boolean cascade);

    /**
     * 获取第一条数据 (默认不进行级联查询)
     *
     * @return obj
     */
    <T extends AbstractEntity> T top();

    /**
     * 获取第一条数据
     *
     * @param cascade 是否级联
     * @return obj
     */
    <T extends AbstractEntity> T top(boolean cascade);

    /**
     * 获取最大值
     *
     * @param field 类型的属性
     * @return 返回对象
     */
    Object max(String field);

    /**
     * 查询总数
     *
     * @return long
     */
    Number count() throws SQLException;

    /**
     * 删除数据
     *
     * @return 影响行数
     */
    int delete() throws SQLException;

    /**
     * 获取一条数据
     *
     * @return 对象
     */
    <T extends AbstractEntity> T unique();

    /**
     * 获取一条数据
     *
     * @param cascade 是否级联查询
     * @return 对象
     */
    <T extends AbstractEntity> T unique(boolean cascade);

    /**
     * 批量更新操作
     *
     * @return 影响行数
     */
    int update() throws SQLException;
}
