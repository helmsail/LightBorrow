package com.helmsail.lightborrow.framework.config;

import com.helmsail.lightborrow.framework.redis.RedisService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 自动配置。提供 RedisService 封装。
 */
@AutoConfiguration
@ConditionalOnClass(StringRedisTemplate.class)
public class RedisConfig {

    @Bean
    @ConditionalOnMissingBean
    public RedisService redisService(StringRedisTemplate stringRedisTemplate) {
        return new RedisService(stringRedisTemplate);
    }
}
