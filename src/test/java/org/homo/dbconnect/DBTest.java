package org.homo.dbconnect;

import org.homo.Application;
import org.homo.authority.model.User;
import org.homo.dbconnect.config.AbstractDatabaseConfig;
import org.homo.dbconnect.query.Query;
import org.homo.dbconnect.inventory.Session;
import org.homo.dbconnect.inventory.SessionFactory;
import org.homo.dbconnect.transaction.Transaction;
import org.homo.orderdemo.model.Commodity;
import org.homo.orderdemo.model.Order;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wujianchuan 2018/12/26
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class DBTest {

    @Autowired
    Map<String, AbstractDatabaseConfig> databaseConfigMap;

    @Before
    public void setup() {
        databaseConfigMap.forEach((k, v) -> {
            SessionFactory.register(v);
        });
    }

    @Test
    public void test1() {
        Session session = SessionFactory.getSession("mysql");
        Transaction transaction = session.getTransaction();
        transaction.connect();
        Connection connection = transaction.getConnection();
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        try {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement("SELECT T.AVATAR AS avatar, T.NAME AS name FROM TBL_USER T");
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
        Session manager = SessionFactory.getSession("mysql");
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
        Session manager = SessionFactory.getSession("mysql");
        manager.getTransaction().connect();
        Query query = manager.createSQLQuery("select avatar, name from TBL_user");
        Object[] result = (Object[]) query.unique();
        System.out.println(result[0] + "-" + result[1]);
        manager.getTransaction().closeConnection();
    }

    @Test
    public void test4() throws Exception {
        Session manager = SessionFactory.getSession("mysql");
        manager.getTransaction().connect();
        System.out.println(((User) manager.findOne(User.class, 5L)).getName());
        manager.getTransaction().closeConnection();
    }

    @Test
    public void test5() throws Exception {
        Session manager = SessionFactory.getSession("mysql");
        manager.getTransaction().connect();
        User user = (User) manager.findOne(User.class, 5L);
        Order order = (Order) manager.findOne(Order.class, 1L);
        order.setPrice(order.getPrice().add(new BigDecimal("1")));
        manager.update(order);
        manager.getTransaction().closeConnection();
    }

    @Test
    public void test6() throws Exception {
        Session manager = SessionFactory.getSession("mysql");
        manager.getTransaction().connect();
        User user = (User) manager.findOne(User.class, manager.getMaxUuid(User.class));
        manager.delete(user);
        manager.getTransaction().closeConnection();
    }

    @Test
    public void test7() throws Exception {
        Session manager = SessionFactory.getSession("mysql");
        manager.getTransaction().connect();
        manager.getTransaction().transactionOn();
        Order order = Order.newInstance("A-002", new BigDecimal("12.6"));
        List<Commodity> detailList = new ArrayList<>();
        Commodity apple = new Commodity();
        apple.setName("苹果");
        apple.setPrice(new BigDecimal(7.6));
        Commodity cookies = new Commodity();
        cookies.setName("饼干");
        cookies.setPrice(new BigDecimal(5));
        detailList.add(apple);
        detailList.add(cookies);
        order.setCommodities(detailList);
        manager.save(order);
        manager.getTransaction().commit();
        manager.getTransaction().closeConnection();
    }

    @Test
    public void test8() throws Exception {
        Session manager = SessionFactory.getSession("mysql");
        manager.getTransaction().connect();
        System.out.println(((Order) manager.findOne(Order.class, 11L)).getCommodities());
        manager.getTransaction().closeConnection();
    }
}
