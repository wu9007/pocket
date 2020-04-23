package org.hv.pocket.flib;

import org.hv.pocket.function.PocketBiFunction;
import org.hv.pocket.model.MapperFactory;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author wujianchuan
 */
public class ResultSetHandler {
    private ResultSet resultSet;

    private ResultSetHandler(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public static ResultSetHandler newInstance(ResultSet resultSet) {
        return new ResultSetHandler(resultSet);
    }

    public Object getColumnValue(Field field) throws SQLException {
        field.setAccessible(true);
        return ResultSetFunctionLib.get(field.getType().getName()).apply(resultSet, field.getName());
    }

    public Object getMappingColumnValue(Class clazz, Field field) throws SQLException {
        field.setAccessible(true);
        PocketBiFunction<ResultSet, String, Object> strategy = ResultSetFunctionLib.get(field.getType().getName());
        if (strategy == null) {
            throw new IllegalArgumentException(String.format("No strategy for parsing this field type: <<%s>> was found, Please submit issue here: https://github.com/leyan95/pocket/issues", field.getType()));
        }
        return strategy.apply(resultSet, MapperFactory.getViewColumnName(clazz.getName(), field.getName()));
    }
}
