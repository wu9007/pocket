package org.homo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author wujianchuan 2018/12/25
 */
@SpringBootApplication
@EnableCaching(proxyTargetClass = true)
public class Application {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner init() {
        return args -> System.out.println("启动程序");
    }

    @Bean
    public ServletContextListener contextListener() {
        return new ServletContextListener() {
            @Override
            public void contextInitialized(ServletContextEvent sce) {
                System.out.println("监听启动");
            }

            @Override
            public void contextDestroyed(ServletContextEvent sce) {
                System.out.println("监听关闭");
            }
        };
    }
}
