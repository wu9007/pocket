package org.hv.pocket.logger;

import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.function.PocketFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author wujianchuan
 */
public class StatementProxy {

    private final Logger logger = LoggerFactory.getLogger(StatementProxy.class);
    private final DatabaseNodeConfig databaseConfig;

    private StatementProxy(DatabaseNodeConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public static StatementProxy newInstance(DatabaseNodeConfig databaseConfig) {
        return new StatementProxy(databaseConfig);
    }

    public <R> R executeWithLog(PreparedStatement preparedStatement, PocketFunction<PreparedStatement, R> function) throws SQLException {
        long startTime = System.currentTimeMillis();
        R result = function.apply(preparedStatement);
        long endTime = System.currentTimeMillis();
        if (this.databaseConfig.getShowSql()) {
            this.logger.info("Sql: {} \n Milliseconds: {}", preparedStatement.toString(), endTime - startTime);
        }
        return result;
    }
}
