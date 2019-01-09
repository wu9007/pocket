package org.homo.dbconnect;

import org.homo.Application;
import org.homo.authority.model.User;
import org.homo.dbconnect.query.Query;
import org.homo.dbconnect.inventory.InventoryManager;
import org.homo.dbconnect.inventory.InventoryFactory;
import org.homo.dbconnect.transaction.Transaction;
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
    InventoryFactory sessionFactory;

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
            while (resultSet.next()) {
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
    public void test2() throws Exception {
        InventoryManager manager = sessionFactory.getManager();
        Transaction transaction = manager.getTransaction();
        transaction.connect();
        transaction.transactionOn();
        manager.save(User.newInstance("Crease", "克里斯"));
        manager.save(User.newInstance("Homo", "霍姆"));
        transaction.commit();
        transaction.closeConnection();
    }

    @Test
    public void test3() throws SQLException {
        InventoryManager manager = sessionFactory.getManager();
        manager.getTransaction().connect();
        Query query = manager.createSQLQuery("select avatar, name from TBL_user");
        Object[] result = (Object[]) query.unique();
        System.out.println(result[0] + "-" + result[1]);
        manager.getTransaction().closeConnection();
    }

    @Test
    public void test4() throws Exception {
        InventoryManager manager = sessionFactory.getManager();
        manager.getTransaction().connect();
        System.out.println(((User) manager.findOne(User.class, 1L)).getName());
        manager.getTransaction().closeConnection();
    }

    @Test
    public void test5() throws Exception {
        InventoryManager manager = sessionFactory.getManager();
        manager.getTransaction().connect();
        User user = (User) manager.findOne(User.class, 1L);
        user.setAvatar("ANT");
        user.setName("蚂蚁2号");
        manager.update(user);
        manager.getTransaction().closeConnection();
    }

    @Test
    public void test6() throws Exception {
        InventoryManager manager = sessionFactory.getManager();
        manager.getTransaction().connect();
        User user = (User) manager.findOne(User.class, manager.getMaxUuid(User.class));
        manager.delete(user);
        manager.getTransaction().closeConnection();
    }
}
