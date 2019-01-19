package org.hunter.pocket.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

/**
 * @author wujianchuan 2019/1/17
 */
class CacheTemplateSerializer {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    static StringRedisTemplate createStringTemplate(LettuceConnectionFactory factory) {
        Jackson2JsonRedisSerializer<String> stringSerializer = new Jackson2JsonRedisSerializer<>(String.class);
        stringSerializer.setObjectMapper(objectMapper);

        StringRedisTemplate template = new StringRedisTemplate(factory);
        template.setValueSerializer(stringSerializer);
        template.afterPropertiesSet();
        return template;
    }

    static RedisTemplate<String, Object> createObjectTemplate(LettuceConnectionFactory factory) {
        Jackson2JsonRedisSerializer<Object> objectSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        objectSerializer.setObjectMapper(objectMapper);

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setValueSerializer(objectSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
