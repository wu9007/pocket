package org.homo.pocket.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author wujianchuan 2019/1/17
 */
@Configuration
public class LogicCacheConfig {

    @Bean
    @ConfigurationProperties(prefix = "pocket.cache.logic")
    public RedisStandaloneConfiguration logicRedisConfig() {
        return new RedisStandaloneConfiguration();
    }

    @Bean
    @ConditionalOnBean(name = "logicCacheConfig")
    public LettuceConnectionFactory logicRedisConnectionFactory(@Qualifier(value = "logicRedisConfig") RedisStandaloneConfiguration logicRedisConfig) {
        return new LettuceConnectionFactory(logicRedisConfig);
    }

    @Bean
    @ConditionalOnBean(name = "logicRedisConnectionFactory")
    public StringRedisTemplate logicStringRedisTemplate(@Qualifier(value = "logicRedisConnectionFactory") LettuceConnectionFactory logicRedisConnectionFactory) {
        return CacheTemplateSerializer.createStringTemplate(logicRedisConnectionFactory);
    }

    @Bean
    @ConditionalOnBean(name = "logicRedisConnectionFactory")
    public RedisTemplate<String, Object> logicRedisTemplate(@Qualifier(value = "logicRedisConnectionFactory") LettuceConnectionFactory logicRedisConnectionFactory) {
        return CacheTemplateSerializer.createObjectTemplate(logicRedisConnectionFactory);
    }
}
