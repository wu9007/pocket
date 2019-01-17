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
public class BenchCacheConfig {

    @Bean
    @ConfigurationProperties(prefix = "pocket.cache.bench")
    public RedisStandaloneConfiguration benchRedisConfig() {
        return new RedisStandaloneConfiguration();
    }

    @Bean
    @ConditionalOnBean(name = "benchCacheConfig")
    public LettuceConnectionFactory benchRedisConnectionFactory(@Qualifier(value = "benchRedisConfig") RedisStandaloneConfiguration benchRedisConfig) {
        return new LettuceConnectionFactory(benchRedisConfig);
    }

    @Bean
    @ConditionalOnBean(name = "benchRedisConnectionFactory")
    public StringRedisTemplate stringBenchRedisTemplate(@Qualifier(value = "benchRedisConnectionFactory") LettuceConnectionFactory benchRedisConnectionFactory) {
        return CacheTemplateSerializer.createStringTemplate(benchRedisConnectionFactory);
    }

    @Bean
    @ConditionalOnBean(name = "benchRedisConnectionFactory")
    public RedisTemplate<String, Object> benchRedisTemplate(@Qualifier(value = "benchRedisConnectionFactory") LettuceConnectionFactory benchRedisConnectionFactory) {
        return CacheTemplateSerializer.createObjectTemplate(benchRedisConnectionFactory);
    }
}
