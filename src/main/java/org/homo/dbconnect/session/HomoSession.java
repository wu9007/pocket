package org.homo.dbconnect.session;

import org.homo.authority.model.User;
import org.homo.core.model.BaseEntity;
import org.homo.dbconnect.transaction.HomoTransaction;
import org.homo.dbconnect.transaction.Transaction;
import org.homo.dbconnect.query.AbstractQuery;
import org.homo.dbconnect.query.HomoQuery;
import org.homo.dbconnect.uuidstrategy.HomoUuidGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author wujianchuan 2019/1/1
 */
public class HomoSession implements Session {

    private Connection connection;

    HomoSession(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Transaction getTransaction() {
        return new HomoTransaction(this.connection);
    }

    @Override
    public int save(BaseEntity entity) throws SQLException {
        User user = (User) entity;
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO USER(UUID, AVATAR, NAME) VALUES(?, ?, ?)");
        preparedStatement.setObject(1, HomoUuidGenerator.getInstance().getUuid(entity.getClass(), this));
        preparedStatement.setObject(2, user.getAvatar());
        preparedStatement.setObject(3, user.getName());
        return preparedStatement.executeUpdate();
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

    @Override
    public AbstractQuery createSQLQuery(String sql) {
        return new HomoQuery(sql, connection);
    }

    @Override
    public long getMaxUuid(Class clazz) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT MAX(UUID) FROM USER");
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getLong(1);
        } else {
            return 0;
        }
    }
}
