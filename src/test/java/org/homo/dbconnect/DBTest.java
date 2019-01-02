package org.homo.dbconnect;

import org.homo.Application;
import org.homo.authority.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author wujianchuan 2018/12/26
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class DBTest {

    @Autowired
    DatabaseManager manager;
    @Autowired
    SessionFactory sessionFactory;

    @Before
    public void setup() {
    }

    @Test
    public void test1() {
        Connection connection = manager.getConn();
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        try {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement("SELECT T.AVATAR AS avatar, T.NAME AS name FROM USER T");
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String avatar = resultSet.getString("avatar");
                String name = resultSet.getString("name");
                User user = User.newInstance(avatar, name);
                System.out.println(user.getName() + " - " + user.getAvatar());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2() throws SQLException {
        UuidProducer producer = HomoUuidProducter.getInstance();
        Session session = sessionFactory.getSession("homo");
        Transaction transaction = session.getTransaction();
        transaction.transactionOn();
        User u1 = User.newInstance("Crease", "克里斯");
        u1.setUuid(producer.getUuid(User.class));
        User u2 = User.newInstance("Poseidon", "波塞东");
        u2.setUuid(producer.getUuid(User.class));
        session.save(u1);
        session.save(u2);
        transaction.commit();
    }
}
