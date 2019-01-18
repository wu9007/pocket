package org.homo.pocket.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author wujianchuan 2019/1/17
 */
@Configuration
public class BaseCacheConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "pocket.cache.base")
    public RedisStandaloneConfiguration baseRedisConfig() {
        return new RedisStandaloneConfiguration();
    }

    @Bean
    @Primary
    @ConditionalOnBean(name = "baseCacheConfig")
    public LettuceConnectionFactory baseRedisConnectionFactory(RedisStandaloneConfiguration baseRedisConfig) {
        return new LettuceConnectionFactory(baseRedisConfig);
    }

    @Bean
    @Primary
    @ConditionalOnBean(name = "baseRedisConnectionFactory")
    public StringRedisTemplate baseStringRedisTemplate(LettuceConnectionFactory baseRedisConnectionFactory) {
        return CacheTemplateSerializer.createStringTemplate(baseRedisConnectionFactory);
    }

    @Bean
    @ConditionalOnBean(name = "baseRedisConnectionFactory")
    public RedisTemplate<String, Object> baseRedisTemplate(LettuceConnectionFactory baseRedisConnectionFactory) {
        return CacheTemplateSerializer.createObjectTemplate(baseRedisConnectionFactory);
    }
}
