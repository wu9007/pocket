package org.hv.pocket.query;

import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.constant.CommonSql;
import org.hv.pocket.constant.SqlOperateTypes;
import org.hv.pocket.criteria.ParameterTranslator;
import org.hv.pocket.criteria.SqlFactory;
import org.hv.pocket.flib.PreparedStatementHandler;
import org.hv.pocket.flib.ResultSetHandler;
import org.hv.pocket.model.MapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hv.pocket.constant.RegexString.SQL_PARAMETER_REGEX;

/**
 * @author wujianchuan 2019/1/3
 */
public class SQLQueryImpl extends AbstractSqlQuery implements SQLQuery {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLQueryImpl.class);

    public SQLQueryImpl(Connection connection, DatabaseNodeConfig databaseNodeConfig) {
        super(connection, databaseNodeConfig);
    }

    public SQLQueryImpl(String sql, Connection connection, DatabaseNodeConfig databaseNodeConfig) {
        super(sql, connection, databaseNodeConfig);
    }

    public SQLQueryImpl(String sql, Connection connection, DatabaseNodeConfig databaseNodeConfig, Class<?> clazz) {
        super(connection, sql, databaseNodeConfig, clazz);
    }

    @Override
    public int execute() throws SQLException {
        return executeUpdate();
    }

    @Override
    public Object unique() throws SQLException {
        ResultSet resultSet = executeQuery();
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
    public <E> List<E> list() throws SQLException {
        if (this.limited()) {
            super.sql = SqlFactory.getInstance().applySql(databaseNodeConfig.getDriverName(), SqlOperateTypes.LIMIT, super.sql, new Integer[]{this.getStart(), this.getLimit()});
        }
        ResultSet resultSet = executeQuery();
        List<E> results = new ArrayList<>();
        while (resultSet.next()) {
            if (clazz != null) {
                try {
                    E result = (E) getEntity(resultSet);
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
    public LocalDateTime now() throws SQLException {
        super.sql = "SELECT " + SqlFactory.getInstance().getSql(super.databaseNodeConfig.getDriverName(), SqlOperateTypes.NOW);
        ResultSet resultSet = executeQuery();
        resultSet.next();
        Timestamp timestamp = getObjects(resultSet);
        return timestamp.toLocalDateTime();
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

    @Override
    public SQLQuery addBatch() throws SQLException {
        this.completePreparedStatement();
        super.batchExecution = true;
        super.preparedStatement.addBatch();
        return this;
    }

    @Override
    public int[] executeBatch() throws SQLException {
        if (!super.batchExecution) {
            throw new SQLException("It is not currently in batch execution mode.");
        }
        return super.persistenceProxy.executeWithLog(super.preparedStatement, PreparedStatement::executeBatch, sql);
    }

    private ResultSet executeQuery() throws SQLException {
        this.completePreparedStatement();
        return super.persistenceProxy.executeWithLog(super.preparedStatement, PreparedStatement::executeQuery, sql);
    }

    private int executeUpdate() throws SQLException {
        this.completePreparedStatement();
        return super.persistenceProxy.executeWithLog(super.preparedStatement, PreparedStatement::executeUpdate, sql);
    }

    private void completePreparedStatement() throws SQLException {
        String executeSql = sql;
        if (!parameterMap.isEmpty()) {
            LOGGER.debug(parameterMap.entrySet().stream().map((item) -> "<" + item.getKey() + ":" + item.getValue() + ">").collect(Collectors.joining("\t")));
        }
        if (super.parameterMap.size() > 0) {
            Pattern pattern = Pattern.compile(SQL_PARAMETER_REGEX);
            Matcher matcher = pattern.matcher(sql);
            while (matcher.find()) {
                String regexString = matcher.group();
                String name = regexString.substring(1);
                if (!super.parameterMap.containsKey(name)) {
                    throw new SQLException(String.format("Parameter %s was not found", name));
                }
                Object parameter = super.parameterMap.get(name);
                if (parameter instanceof List) {
                    List<?> parameters = (List<?>) parameter;
                    if (preparedStatement == null) {
                        executeSql = executeSql.replaceAll(regexString, parameters.stream().map(item -> CommonSql.PLACEHOLDER).collect(Collectors.joining(",")));
                    }
                    parameters.forEach(item -> super.queryParameters.add(ParameterTranslator.newInstance(item)));
                } else {
                    if (preparedStatement == null) {
                        executeSql = executeSql.replaceAll(regexString + ",", CommonSql.PLACEHOLDER + ",")
                                .replaceAll(regexString + "\\)", CommonSql.PLACEHOLDER + "\\)")
                                .replaceAll(regexString + " ", CommonSql.PLACEHOLDER + " ");
                    }
                    super.queryParameters.add(ParameterTranslator.newInstance(parameter));
                }
            }
            if (super.preparedStatement == null) {
                super.preparedStatement = super.connection.prepareStatement(executeSql);
            }
            PreparedStatementHandler.newInstance(super.clazz, super.preparedStatement).completionPreparedStatement(super.queryParameters);
            super.parameterMap.clear();
            super.queryParameters.clear();
        } else {
            if (super.preparedStatement == null) {
                super.preparedStatement = super.connection.prepareStatement(executeSql);
            }
        }
    }

    private <T> T getObjects(ResultSet resultSet) throws SQLException {
        int columnNameSize = this.columnNameList.size();
        int columnSize = resultSet.getMetaData().getColumnCount();
        if (columnNameSize != columnSize) {
            if (columnNameSize == 0 && columnSize == 1) {
                return (T) resultSet.getObject(1);
            }
            throw new SQLException("Column mapping failed");
        } else {
            Map<String, Object> result = new LinkedHashMap<>();
            for (int nameIndex = 0, columnIndex = 1; nameIndex < columnNameSize; nameIndex++, columnIndex++) {
                result.put(this.columnNameList.get(nameIndex), resultSet.getObject(columnIndex));
            }
            return (T) result;
        }
    }

    private Object getEntity(ResultSet resultSet) throws InstantiationException, IllegalAccessException, SQLException {
        Object result = clazz.newInstance();
        List<Field> fields = Arrays.stream(MapperFactory.getViewFields(clazz.getName()))
                .filter(field -> this.sql.contains(field.getName() + ",") || this.sql.contains(field.getName() + " "))
                .collect(Collectors.toList());
        ResultSetHandler resultSetHandler = ResultSetHandler.newInstance(resultSet);
        for (Field field : fields) {
            field.set(result, resultSetHandler.getColumnValue(field));
        }
        return result;
    }
}
