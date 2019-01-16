package org.homo.pocket.lunch;

import org.homo.pocket.config.DatabaseConfig;
import org.homo.pocket.connect.ConnectionManager;
import org.homo.pocket.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author wujianchuan 2019/1/12
 */
@Component
@Order(value = 0)
public class DatabaseLauncher implements CommandLineRunner {
    private final
    DatabaseConfig databaseConfig;

    @Autowired
    public DatabaseLauncher(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    @Override
    public void run(String... args) {
        ConnectionManager connectionFactory = ConnectionManager.getInstance();
        connectionFactory.register(databaseConfig);
        SessionFactory.register(databaseConfig);
    }
}
