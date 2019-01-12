package org.homo.dbconnect.inventory;

import org.homo.dbconnect.config.AbstractDatabaseConfig;
import org.homo.dbconnect.criteria.AbstractCriteria;
import org.homo.dbconnect.transaction.TransactionImpl;
import org.homo.dbconnect.transaction.Transaction;
import org.homo.dbconnect.query.AbstractQuery;
import org.homo.dbconnect.query.HomoQuery;

/**
 * @author wujianchuan 2019/1/1
 */
public class SessionImpl extends AbstractSession {

    SessionImpl(AbstractDatabaseConfig databaseConfig) {
        this.transaction = new TransactionImpl(databaseConfig);
    }

    @Override
    public String getDbName() {
        return MYSQL_DB_NAME;
    }

    @Override
    public Transaction getTransaction() {
        return this.transaction;
    }

    @Override
    public AbstractQuery createSQLQuery(String sql) {
        return new HomoQuery(sql, this.transaction.getConnection());
    }

    @Override
    public AbstractCriteria creatCriteria(Class clazz) {
        return null;
    }
}
