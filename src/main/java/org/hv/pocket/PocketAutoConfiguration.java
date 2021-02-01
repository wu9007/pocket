package org.hv.pocket;

import org.hv.pocket.config.DatabaseConfig;
import org.hv.pocket.identify.IdentifyGenerator;
import org.hv.pocket.identify.IncrementLongGenerator;
import org.hv.pocket.identify.IncrementStrGenerator;
import org.hv.pocket.lunch.PocketConfigDefault;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 对象持久化映射工具自动配置类
 *
 * @author 吴建川
 * @version 0.0.1
 * @date 2021/01/28
 */
@Configuration
@EnableConfigurationProperties({DatabaseConfig.class})
@Import({PocketConfigDefault.class})
public class PocketAutoConfiguration {

    @Bean
    public IdentifyGenerator incrementLongGenerator() {
        return new IncrementLongGenerator();
    }

    @Bean
    public IdentifyGenerator incrementStrGenerator() {
        return new IncrementStrGenerator();
    }
}
