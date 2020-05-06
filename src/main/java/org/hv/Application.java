package org.hv;

import org.hv.pocket.lunch.PocketConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author wujianchuan 2018/12/25
 */
@SpringBootApplication
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner getCommandLineRunner() {
        return new CommandLineRunner() {
            @Autowired
            private PocketConfig pocketConfig;

            @Override
            public void run(String... args) throws Exception {
                this.pocketConfig.init();
            }
        };
    }
}
