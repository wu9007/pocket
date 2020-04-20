package org.hv.pocket.query;

import com.mysql.cj.jdbc.result.ResultSetImpl;
import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.constant.CommonSql;
import org.hv.pocket.criteria.ParameterTranslator;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.utils.FieldTypeStrategy;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hv.pocket.constant.RegexString.SQL_PARAMETER_REGEX;

/**
 * @author wujianchuan 2019/1/3
 */
public class SQLQueryImpl extends AbstractSQLQuery implements SQLQuery {

    private final FieldTypeStrategy fieldTypeStrategy = FieldTypeStrategy.getInstance();

    public SQLQueryImpl(String sql, Connection connection, DatabaseNodeConfig databaseNodeConfig) {
        super(sql, connection, databaseNodeConfig);
    }

    public SQLQueryImpl(String sql, Connection connection, DatabaseNodeConfig databaseNodeConfig, Class clazz) {
        super(connection, sql, databaseNodeConfig, clazz);
    }

    @Override
    public Object unique() throws SQLException {
        ResultSet resultSet = execute(sql);
        if (resultSet.next()) {
            if (clazz != null) {
                try {
                    return getEntity(resultSet);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new IllegalAccessError();
                }
            } else {
                return getObjects(resultSet);
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
            if (clazz != null) {
                try {
                    Object result = getEntity(resultSet);
                    results.add(result);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new IllegalAccessError();
                }
            } else {
                results.add(getObjects(resultSet));
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

    @Override
    public SQLQuery mapperColumn(String... columnNames) {
        this.columnNameList.addAll(Arrays.asList(columnNames));
        return this;
    }

    private ResultSet execute(String sql) throws SQLException {
        PreparedStatement preparedStatement;
        String executeSql = sql;
        if (this.parameterMap.size() > 0) {
            List<ParameterTranslator> queryParameters = new LinkedList<>();
            Pattern pattern = Pattern.compile(SQL_PARAMETER_REGEX);
            Matcher matcher = pattern.matcher(sql);
            while (matcher.find()) {
                String regexString = matcher.group();
                String name = regexString.substring(1);
                Object parameter = this.parameterMap.get(name);
                if (parameter instanceof List) {
                    List<Object> parameters = (List<Object>) parameter;
                    executeSql = executeSql.replaceAll(regexString, parameters.stream().map(item -> CommonSql.PLACEHOLDER).collect(Collectors.joining(",")));
                    parameters.forEach(item -> queryParameters.add(ParameterTranslator.newInstance(item)));
                } else {
                    executeSql = executeSql.replaceAll(regexString, CommonSql.PLACEHOLDER);
                    queryParameters.add(ParameterTranslator.newInstance(parameter));
                }
            }
            preparedStatement = this.connection.prepareStatement(executeSql);
            fieldTypeStrategy.setPreparedStatement(preparedStatement, queryParameters);
        } else {
            preparedStatement = this.connection.prepareStatement(executeSql);
        }
        return super.statementProxy.executeWithLog(preparedStatement, PreparedStatement::executeQuery);
    }

    private Object getObjects(ResultSet resultSet) throws SQLException {
        int columnNameSize = this.columnNameList.size();
        int columnSize = ((ResultSetImpl) resultSet).getColumnDefinition().getFields().length;
        if (columnNameSize != columnSize) {
            if (columnNameSize == 0 && columnSize == 1) {
                return resultSet.getObject(1);
            }
            throw new SQLException("Column mapping failed");
        } else {
            Map<String, Object> result = new LinkedHashMap<>();
            for (int nameIndex = 0, columnIndex = 1; nameIndex < columnNameSize; nameIndex++, columnIndex++) {
                result.put(this.columnNameList.get(nameIndex), resultSet.getObject(columnIndex));
            }
            return result;
        }
    }

    private Object getEntity(ResultSet resultSet) throws InstantiationException, IllegalAccessException {
        Object result = clazz.newInstance();
        List<Field> fields = Arrays.stream(MapperFactory.getViewFields(clazz.getName()))
                .filter(field -> this.sql.contains(field.getName() + ",") || this.sql.contains(field.getName() + " "))
                .collect(Collectors.toList());
        for (Field field : fields) {
            field.set(result, fieldTypeStrategy.getColumnValue(field, resultSet));
        }
        return result;
    }
}
