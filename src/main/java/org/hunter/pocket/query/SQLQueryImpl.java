package org.hunter.pocket.query;

import org.hunter.pocket.utils.FieldTypeStrategy;
import org.hunter.pocket.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wujianchuan 2019/1/3
 */
public class SQLQueryImpl extends AbstractSQLQuery implements SQLQuery {

    private final FieldTypeStrategy fieldTypeStrategy = FieldTypeStrategy.getInstance();
    private final ReflectUtils reflectUtils = ReflectUtils.getInstance();

    public SQLQueryImpl(String sql, Connection connection) {
        super(sql, connection);
    }

    public SQLQueryImpl(String sql, Connection connection, Class clazz) {
        super(connection, sql, clazz);
    }

    @Override
    public Object unique() throws SQLException {
        PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
        String str = sql.replaceAll("\\s*", "");
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            String[] columnNames = str
                    .substring(str.indexOf("SELECT") + str.indexOf("select") + 6, str.indexOf("FROM") + str.indexOf("from"))
                    .split(",");
            if (clazz != null) {
                try {
                    return getEntity(resultSet, columnNames);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new IllegalAccessError();
                }
            } else {
                return getObjects(resultSet, columnNames);
            }
        } else {
            return null;
        }
    }

    @Override
    public List list() throws SQLException {
        PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
        String str = sql.replaceAll("\\s*", "");
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Object> results = new ArrayList<>();
        while (resultSet.next()) {
            String[] columnNames = str
                    .substring(str.indexOf("SELECT") + str.indexOf("select") + 6, str.indexOf("FROM") + str.indexOf("from"))
                    .split(",");
            if (clazz != null) {
                try {
                    Object result = getEntity(resultSet, columnNames);
                    results.add(result);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new IllegalAccessError();
                }
            } else {
                results.add(getObjects(resultSet, columnNames));
            }
        }
        return results;
    }

    private Object[] getObjects(ResultSet resultSet, String[] columnNames) throws SQLException {
        int columnCount = columnNames.length;
        Object[] result = new Object[columnCount];
        for (int index = 1; index <= columnCount; index++) {
            result[index - 1] = resultSet.getObject(index);
        }
        return result;
    }

    private Object getEntity(ResultSet resultSet, String[] columnNames) throws InstantiationException, IllegalAccessException {
        Object result = clazz.newInstance();
        List<Field> fields = Arrays.stream(reflectUtils.getFields(clazz))
                .filter(field -> Arrays.asList(columnNames).contains(field.getName()))
                .collect(Collectors.toList());
        for (Field field : fields) {
            field.set(result, fieldTypeStrategy.getColumnValue(field, resultSet));
        }
        return result;
    }
}
