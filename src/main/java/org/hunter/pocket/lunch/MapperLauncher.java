package org.hunter.pocket.lunch;

import org.hunter.pocket.model.MapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author wujianchuan
 */
@Component
@Order(value = -99)
public class MapperLauncher implements CommandLineRunner {
    private final ApplicationContext context;

    @Autowired
    public MapperLauncher(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run(String... args) {
        MapperFactory.init(context);
    }
}
