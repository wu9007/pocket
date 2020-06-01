package org.hv.pocket.query;

import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.exception.QueryException;
import org.hv.pocket.model.AbstractEntity;

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
public class ProcessQueryImpl<T extends AbstractEntity> extends AbstractSqlQuery implements ProcessQuery<T> {

    private String[] parameters;

    public ProcessQueryImpl(String processSQL, Connection connection, DatabaseNodeConfig databaseNodeConfig) {
        super(processSQL, connection, databaseNodeConfig);
    }

    @Override
    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public T unique(Function<ResultSet, T> rowMapperFunction) {
        ResultSet resultSet;
        try {
            resultSet = this.execute();
            if (resultSet.next()) {
                return rowMapperFunction.apply(resultSet);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new QueryException(e.getMessage(), e, true, true);
        }
    }

    @Override
    public List<T> list(Function<ResultSet, T> rowMapperFunction) {
        List<T> resultList = new ArrayList<>();
        ResultSet resultSet;
        try {
            resultSet = this.execute();
            while (resultSet.next()) {
                resultList.add(rowMapperFunction.apply(resultSet));
            }
        } catch (SQLException e) {
            throw new QueryException(e.getMessage(), e, true, true);
        }
        return resultList;
    }

    private ResultSet execute() {
        try {
            CallableStatement callableStatement = connection.prepareCall(this.sql);
            for (int index = 0; index < parameters.length; index++) {
                callableStatement.setString(index + 1, parameters[index]);
            }
            callableStatement.execute();
            return callableStatement.getResultSet();
        } catch (SQLException e) {
            throw new QueryException(e.getMessage(), e, true, true);
        }
    }
}
