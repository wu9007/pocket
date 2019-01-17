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
public class MainCacheConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "pocket.cache.main")
    public RedisStandaloneConfiguration mainRedisConfig() {
        return new RedisStandaloneConfiguration();
    }

    @Bean
    @Primary
    @ConditionalOnBean(name = "mainCacheConfig")
    public LettuceConnectionFactory mainRedisConnectionFactory(RedisStandaloneConfiguration mainRedisConfig) {
        return new LettuceConnectionFactory(mainRedisConfig);
    }

    @Bean
    @Primary
    @ConditionalOnBean(name = "mainRedisConnectionFactory")
    public StringRedisTemplate mainStringRedisTemplate(LettuceConnectionFactory mainRedisConnectionFactory) {
        return CacheTemplateSerializer.createStringTemplate(mainRedisConnectionFactory);
    }

    @Bean
    @ConditionalOnBean(name = "mainRedisConnectionFactory")
    public RedisTemplate<String, Object> mainRedisTemplate(LettuceConnectionFactory mainRedisConnectionFactory) {
        return CacheTemplateSerializer.createObjectTemplate(mainRedisConnectionFactory);
    }
}
