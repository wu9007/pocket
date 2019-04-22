package org.hunter.pocket.query;

import org.hunter.pocket.constant.CommonSql;
import org.hunter.pocket.criteria.ParameterTranslator;
import org.hunter.pocket.utils.FieldTypeStrategy;
import org.hunter.pocket.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hunter.pocket.constant.RegexString.SQL_PARAMETER_REGEX;

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
        ResultSet resultSet = execute(sql);
        if (resultSet.next()) {
            List<String> columnNames = this.getColumnNames();
            if (clazz != null) {
                try {
                    return getEntity(resultSet, columnNames);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new IllegalAccessError();
                }
            } else {
                return getObjects(resultSet, columnNames.size());
            }
        } else {
            return null;
        }
    }

    @Override
    public List list() throws SQLException {
        StringBuilder querySQL = new StringBuilder(this.sql);
        if (this.limited()) {
            querySQL.append(" LIMIT ")
                    .append(this.getStart())
                    .append(CommonSql.COMMA)
                    .append(this.getLimit());
        }
        ResultSet resultSet = execute(querySQL.toString());
        List<Object> results = new ArrayList<>();
        while (resultSet.next()) {
            List<String> columnNames = this.getColumnNames();
            if (clazz != null) {
                try {
                    Object result = getEntity(resultSet, columnNames);
                    results.add(result);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new IllegalAccessError();
                }
            } else {
                results.add(getObjects(resultSet, columnNames.size()));
            }
        }
        return results;
    }

    @Override
    public SQLQuery limit(int start, int limit) {
        this.setLimit(start, limit);
        return this;
    }

    @Override
    public SQLQuery setParameter(String key, Object value) {
        this.parameterMap.put(key, value);
        return this;
    }

    private ResultSet execute(String sql) throws SQLException {
        String executeSql = sql.replaceAll(SQL_PARAMETER_REGEX, CommonSql.PLACEHOLDER);
        PreparedStatement preparedStatement = this.connection.prepareStatement(executeSql);
        if (this.parameterMap.size() > 0) {
            List<ParameterTranslator> queryParameters = new LinkedList<>();
            Pattern pattern = Pattern.compile(SQL_PARAMETER_REGEX);
            Matcher matcher = pattern.matcher(sql);
            while (matcher.find()) {
                String name = matcher.group().substring(1);
                queryParameters.add(ParameterTranslator.newInstance(name, this.parameterMap.get(name)));
            }
            fieldTypeStrategy.setPreparedStatement(preparedStatement, queryParameters);
        }
        return preparedStatement.executeQuery();
    }

    private Object[] getObjects(ResultSet resultSet, int columnCount) throws SQLException {
        Object[] result = new Object[columnCount];
        for (int index = 1; index <= columnCount; index++) {
            result[index - 1] = resultSet.getObject(index);
        }
        return result;
    }

    private List<String> getColumnNames() {
        String str = this.sql.replaceAll("\\s*", "");
        String[] columnStrArray = str
                .substring(str.toUpperCase().indexOf("SELECT") + 7, str.toUpperCase().indexOf("FROM"))
                .split(CommonSql.COMMA);
        return Arrays
                .stream(columnStrArray)
                .map(columnStr -> columnStr.substring(columnStr.toUpperCase().indexOf("AS") + 2))
                .collect(Collectors.toList());
    }

    private Object getEntity(ResultSet resultSet, List<String> columnNames) throws InstantiationException, IllegalAccessException {
        Object result = clazz.newInstance();
        List<Field> fields = Arrays.stream(reflectUtils.getFields(clazz))
                .filter(field -> columnNames.contains(field.getName()))
                .collect(Collectors.toList());
        for (Field field : fields) {
            field.set(result, fieldTypeStrategy.getColumnValue(field, resultSet));
        }
        return result;
    }
}
