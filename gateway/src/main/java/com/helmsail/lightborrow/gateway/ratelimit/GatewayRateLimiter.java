package com.helmsail.lightborrow.gateway.ratelimit;

import com.helmsail.lightborrow.gateway.config.GatewayProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RecoveryStrategy;
import io.github.bucket4j.redis.redisson.Bucket4jRedisson;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 基于 Bucket4j 令牌桶算法的个人限流器。
 *
 * <p>每个用户独立一个令牌桶（key = {@code ratelimit:{userId}}），使用 Redisson 实现
 * 分布式状态同步，支持多实例部署。令牌以固定速率补充，突发时可消耗累积令牌。
 *
 * <p>配置项：
 * <ul>
 *   <li>{@code lightborrow.gateway.rate-limit-window-seconds} — 补充周期（秒），默认 60</li>
 *   <li>{@code lightborrow.gateway.rate-limit-max-requests} — 周期内最大请求数，默认 20</li>
 * </ul>
 */
@Slf4j
@Component
@ConditionalOnBean(RedissonClient.class)
public class GatewayRateLimiter {

    private static final String RATE_LIMIT_KEY_PREFIX = "ratelimit:";

    private final GatewayProperties gatewayProperties;
    private final ProxyManager<String> proxyManager;

    public GatewayRateLimiter(RedissonClient redissonClient, GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;

        if (!(redissonClient instanceof Redisson)) {
            throw new IllegalArgumentException("RedissonClient must be a Redisson instance");
        }
        CommandAsyncExecutor executor = ((Redisson) redissonClient).getCommandExecutor();
        this.proxyManager = Bucket4jRedisson.casBasedBuilder(executor)
                .defaultRecoveryStrategy(RecoveryStrategy.RECONSTRUCT)
                .build();

        log.info("[Gateway] Bucket4j 限流器初始化完成");
    }

    /**
     * 检查指定用户的请求是否允许通过。
     * <p>从令牌桶中消费 1 个令牌，令牌充足则放行，不足则拒绝。
     *
     * @param userId 用户 ID
     * @return true 允许通过，false 被限流拒绝
     */
    public boolean allowRequest(String userId) {
        try {
            Bucket bucket = buildBucket(userId);
            if (bucket.tryConsume(1)) {
                return true;
            }

            log.warn("[Gateway] 用户 {} 触发限流，capacity={}, window={}s",
                    userId,
                    gatewayProperties.getRateLimitMaxRequests(),
                    gatewayProperties.getRateLimitWindowSeconds());
            return false;

        } catch (Exception e) {
            log.error("[Gateway] 限流检查异常 userId={}", userId, e);
            return true; // 降级：限流失败时放行
        }
    }

    private Bucket buildBucket(String userId) {
        int capacity = gatewayProperties.getRateLimitMaxRequests();
        int windowSeconds = gatewayProperties.getRateLimitWindowSeconds();

        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, Duration.ofSeconds(windowSeconds))
                .build();

        BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(limit)
                .build();

        return proxyManager.builder()
                .build(RATE_LIMIT_KEY_PREFIX + userId, config);
    }
}
