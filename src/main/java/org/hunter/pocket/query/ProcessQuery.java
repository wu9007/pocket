package org.hunter.pocket.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

/**
 * @author wujianchuan 2019/2/14
 */
public interface ProcessQuery<T> {

    void setParameters(String[] parameters);

    T unique(Function<ResultSet, T> rowMapperFunction) throws SQLException;

    List<T> list(Function<ResultSet, T> rowMapperFunction) throws SQLException;
}
