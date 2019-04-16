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
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        OBJECT_MAPPER.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    static StringRedisTemplate createStringTemplate(LettuceConnectionFactory factory) {
        Jackson2JsonRedisSerializer<String> stringSerializer = new Jackson2JsonRedisSerializer<>(String.class);
        stringSerializer.setObjectMapper(OBJECT_MAPPER);

        StringRedisTemplate template = new StringRedisTemplate(factory);
        template.setValueSerializer(stringSerializer);
        template.afterPropertiesSet();
        return template;
    }

    static RedisTemplate<String, Object> createObjectTemplate(LettuceConnectionFactory factory) {
        Jackson2JsonRedisSerializer<Object> objectSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        objectSerializer.setObjectMapper(OBJECT_MAPPER);

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setValueSerializer(objectSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
