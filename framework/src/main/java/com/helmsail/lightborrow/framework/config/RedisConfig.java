package com.helmsail.lightborrow.framework.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmsail.lightborrow.framework.mq.RedisStreamPublisher;
import com.helmsail.lightborrow.framework.redis.RedisService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 自动配置。使用 GenericJackson2JsonRedisSerializer 写入 {@literal @class} 类型信息，
 * 避免 Jackson2JsonRedisSerializer 的 LinkedHashMap 类型丢失问题。
 */
@AutoConfiguration
@ConditionalOnClass({RedisTemplate.class, ObjectMapper.class})
public class RedisConfig {

    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                       ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // GenericJackson2JsonRedisSerializer 自动写入 @class 类型信息，反序列化时保持具体类型
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisService redisService(StringRedisTemplate stringRedisTemplate) {
        return new RedisService(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisStreamPublisher redisStreamPublisher(StringRedisTemplate stringRedisTemplate) {
        return new RedisStreamPublisher(stringRedisTemplate);
    }
}
