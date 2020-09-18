package org.hv.pocket.flib;

import org.hv.pocket.function.PocketBiFunction;
import org.hv.pocket.model.MapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author wujianchuan
 */
public class ResultSetHandler {
    private final ResultSet resultSet;
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultSetHandler.class);

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

    public Object getMappingColumnValue(Class<?> clazz, Field field) throws SQLException {
        field.setAccessible(true);
        PocketBiFunction<ResultSet, String, Object> strategy = ResultSetFunctionLib.get(field.getType().getName());
        if (strategy == null) {
            throw new IllegalArgumentException(String.format("No strategy for parsing this field type: <<%s>> was found, Please submit issue here: https://github.com/leyan95/pocket/issues", field.getType()));
        }
        String columnName = MapperFactory.getViewColumnName(clazz.getName(), field.getName());
        try {
            return strategy.apply(resultSet, columnName);
        } catch (SQLException e) {
            LOGGER.warn("Check the {} field of the mapped database column {} in type {}", field.getName(), columnName, clazz.getName());
            throw e;
        }
    }
}
