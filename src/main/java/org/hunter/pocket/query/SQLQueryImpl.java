package org.hunter.pocket.query;

import org.hunter.pocket.utils.FieldTypeStrategy;
import org.hunter.pocket.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        String str = sql.replaceAll("\\s*", "").toUpperCase();
        int columnCount = str
                .substring(str.indexOf("SELECT") + 6, str.indexOf("FROM"))
                .split(",")
                .length;
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            if (clazz != null) {
                try {
                    Object result = clazz.newInstance();
                    Field[] fields = reflectUtils.getMappingField(clazz);
                    for (Field field : fields) {
                        field.set(result, fieldTypeStrategy.getColumnValue(field, resultSet));
                    }
                    return result;
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new IllegalAccessError();
                }

            } else {
                Object[] result = new Object[columnCount];
                for (int index = 1; index <= columnCount; index++) {
                    result[index - 1] = resultSet.getObject(index);
                }
                return result;
            }
        } else {
            return null;
        }
    }
}
