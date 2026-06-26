package com.helmsail.lightborrow.gateway.config;

import com.helmsail.lightborrow.framework.ratelimit.RateLimiter;
import com.helmsail.lightborrow.gateway.ratelimit.GatewayRateLimiter;
import com.helmsail.lightborrow.gateway.ratelimit.LocalRateLimiter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway 模块自动配置。注册 {@link RateLimiter} 限流器 Bean。
 */
@AutoConfiguration
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayAutoConfiguration {

    /**
     * 本地内存限流器（降级方案）。无 Redis 依赖，单机部署时使用。
     */
    @Bean
    @ConditionalOnMissingBean(RateLimiter.class)
    public LocalRateLimiter localRateLimiter(GatewayProperties gatewayProperties) {
        return new LocalRateLimiter(gatewayProperties);
    }

    /**
     * Redis 分布式限流器。仅在 Redisson 在类路径时注册。
     * 拆分为独立配置类以避免 RedissonClient 类不存在时 Spring 内省失败。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.redisson.api.RedissonClient")
    static class RedissonRateLimiterConfiguration {

        @Bean
        @ConditionalOnBean(RedissonClient.class)
        @ConditionalOnMissingBean(RateLimiter.class)
        public GatewayRateLimiter gatewayRateLimiter(RedissonClient redissonClient,
                                                     GatewayProperties gatewayProperties) {
            return new GatewayRateLimiter(redissonClient, gatewayProperties);
        }
    }
}
