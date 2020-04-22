package org.hv.pocket.query;

import org.hv.pocket.model.AbstractEntity;

import java.sql.ResultSet;
import java.util.List;
import java.util.function.Function;

/**
 * @author wujianchuan 2019/2/14
 */
public interface ProcessQuery<T extends AbstractEntity> {

    /**
     * 对条件进行赋值
     *
     * @param parameters 条件以数组的形式进行传递
     */
    void setParameters(String[] parameters);

    /**
     * 查询单条记录
     *
     * @param rowMapperFunction 对象映射
     * @return 要查询的对象
     */
    T unique(Function<ResultSet, T> rowMapperFunction);

    /**
     * 查询多条记录
     *
     * @param rowMapperFunction 对象映射
     * @return 要查询的对象集合
     */
    List<T> list(Function<ResultSet, T> rowMapperFunction);
}
