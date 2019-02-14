package org.hunter.pocket.query;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author wujianchuan 2019/2/14
 */
public class ProcessQueryImpl<T> extends AbstractSQLQuery implements ProcessQuery<T> {

    private String[] parameters;

    public ProcessQueryImpl(String processSQL, Connection connection) {
        super(processSQL, connection);
    }

    @Override
    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public T unique(Function<ResultSet, T> rowMapperFunction) throws SQLException {
        ResultSet resultSet = this.execute();
        if (resultSet.next()) {
            return rowMapperFunction.apply(resultSet);
        }
        return null;
    }

    @Override
    public List<T> list(Function<ResultSet, T> rowMapperFunction) throws SQLException {
        List<T> resultList = new ArrayList<>();
        ResultSet resultSet = this.execute();
        while (resultSet.next()) {
            resultList.add(rowMapperFunction.apply(resultSet));
        }
        return resultList;
    }

    private ResultSet execute() throws SQLException {
        CallableStatement callableStatement = connection.prepareCall(this.sql);
        for (int index = 0; index < parameters.length; index++) {
            callableStatement.setString(index + 1, parameters[index]);
        }
        callableStatement.execute();
        return callableStatement.getResultSet();
    }
}
