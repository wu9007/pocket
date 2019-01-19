package org.hunter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author wujianchuan 2018/12/25
 */
@SpringBootApplication
@EnableCaching(proxyTargetClass = true)
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
