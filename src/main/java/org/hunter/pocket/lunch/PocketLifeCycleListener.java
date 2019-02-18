package org.hunter.pocket.lunch;

import org.hunter.pocket.connect.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

/**
 * @author wujianchuan 2019/2/18
 */
@Configuration
public class PocketLifeCycleListener implements ApplicationListener<ContextClosedEvent> {
    private static Logger logger = LoggerFactory.getLogger(PocketLifeCycleListener.class);

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        logger.info("Destroy all connection.");
        //TODO:how to unregister JDBC driver
        ConnectionManager.getInstance().destroy();
    }
}
