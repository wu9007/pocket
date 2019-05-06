package org.hunter.pocket.criteria.actions;

import java.sql.SQLException;
import java.util.List;

/**
 * @author wujianchuan
 */
public interface OperateDictionary {

    /**
     * 获取所有数据
     *
     * @return 对象集合
     */
    List list();

    /**
     * 获取所有数据,持有条件集合可重复利用
     *
     * @return 对象集合
     */
    List listNotCleanRestrictions();

    /**
     * 获取所有数据
     *
     * @param cascade 是否级联查询
     * @return 对象集合
     */
    List list(boolean cascade);

    /**
     * 获取最大值
     *
     * @param field 类型的属性
     * @return 返回对象
     */
    Object max(String field) throws SQLException;

    /**
     * 查询总数
     *
     * @return long
     */
    long count() throws SQLException;

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
    Object unique() throws SQLException;

    /**
     * 获取一条数据
     *
     * @param cascade 是否级联查询
     * @return 对象
     */
    Object unique(boolean cascade) throws SQLException;

    /**
     * 批量更新操作
     *
     * @return 影响行数
     */
    int update() throws SQLException;
}
