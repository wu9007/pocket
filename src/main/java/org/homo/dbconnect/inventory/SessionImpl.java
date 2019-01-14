package org.homo.dbconnect.inventory;

import org.homo.dbconnect.config.AbstractDatabaseConfig;
import org.homo.dbconnect.criteria.Criteria;
import org.homo.dbconnect.criteria.CriteriaImpl;
import org.homo.dbconnect.transaction.Transaction;
import org.homo.dbconnect.query.AbstractQuery;
import org.homo.dbconnect.query.HomoQuery;

/**
 * @author wujianchuan 2019/1/1
 */
public class SessionImpl extends AbstractSession {

    SessionImpl(AbstractDatabaseConfig databaseConfig) {
        super(databaseConfig);
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
    public Criteria creatCriteria(Class clazz) {
        return new CriteriaImpl(clazz, this.transaction, this.databaseConfig);
    }
}
