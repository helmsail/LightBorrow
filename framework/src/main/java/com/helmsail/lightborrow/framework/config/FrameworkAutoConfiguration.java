package com.helmsail.lightborrow.framework.config;

import com.helmsail.lightborrow.framework.lock.DistributedLockService;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 分布式锁自动配置。独立于 RedisConfig 是为了解耦——无 Redis 时其他能力依然可用。
 */
@AutoConfiguration
@ConditionalOnClass(RedissonClient.class)
public class FrameworkAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DistributedLockService distributedLockService(RedissonClient redissonClient) {
        return new DistributedLockService(redissonClient);
    }
}
