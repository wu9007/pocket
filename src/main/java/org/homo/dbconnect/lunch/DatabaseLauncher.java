package org.homo.dbconnect.lunch;

import org.homo.dbconnect.config.AbstractDatabaseConfig;
import org.homo.dbconnect.connect.ConnectionManager;
import org.homo.dbconnect.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author wujianchuan 2019/1/12
 */
@Component
@Order(value = 0)
public class DatabaseLauncher implements CommandLineRunner {
    private final
    Map<String, AbstractDatabaseConfig> databaseConfigMap;

    @Autowired
    public DatabaseLauncher(Map<String, AbstractDatabaseConfig> databaseConfigMap) {
        this.databaseConfigMap = databaseConfigMap;
    }

    @Override
    public void run(String... args) {
        ConnectionManager connectionFactory = ConnectionManager.getInstance();
        databaseConfigMap.forEach((key, value) -> connectionFactory.register(value));
        databaseConfigMap.forEach((key, value) -> SessionFactory.register(value));
    }
}
