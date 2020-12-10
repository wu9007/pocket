package org.hv;

import org.hv.pocket.exception.PocketMapperException;
import org.hv.pocket.lunch.PocketConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;

/**
 * @author wujianchuan 2018/12/25
 */
@SpringBootApplication
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    Object getPersistenceConfig() {
        return new Object() {
            @Autowired
            private PocketConfig pocketConfig;

            @PostConstruct
            public void run() throws PocketMapperException {
                this.pocketConfig.setDesKey("sward007")
                        .setSm4Key("sward18713839007")
                        .init();
            }
        };
    }
}
