package org.homo.dbconnect;

import org.homo.authority.model.User;
import org.homo.core.model.BaseEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
    public int save(BaseEntity entity) throws SQLException {
        User user = (User) entity;
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO USER(UUID, AVATAR, NAME) VALUES(?, ?, ?)");
        preparedStatement.setObject(1, user.getUuid());
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
}
