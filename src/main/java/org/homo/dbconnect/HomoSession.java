package org.homo.dbconnect;

import org.homo.core.model.BaseEntity;

import java.sql.Connection;

/**
 * @author wujianchuan 2019/1/1
 */
class HomoSession implements Session {

    private Connection connection;

    HomoSession(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Transaction getTransaction() {
        return new HomoTransaction(this.connection);
    }

    @Override
    public int save(BaseEntity entity) {
        return 0;
    }

    @Override
    public int update(BaseEntity entity) {
        return 0;
    }

    @Override
    public int delete(BaseEntity entity) {
        return 0;
    }

    @Override
    public BaseEntity findOne(String uuid) {
        return null;
    }
}
