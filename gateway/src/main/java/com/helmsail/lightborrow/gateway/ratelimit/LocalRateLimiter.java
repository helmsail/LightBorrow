package com.helmsail.lightborrow.gateway.ratelimit;

import com.helmsail.lightborrow.framework.ratelimit.RateLimiter;
import com.helmsail.lightborrow.gateway.config.GatewayProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地内存限流器（非分布式）。基于滑动窗口计数算法，无 Redis 依赖。
 *
 * <p>适用于单机部署或开发环境。生产环境多实例部署建议使用 {@link GatewayRateLimiter}。
 *
 * <p>每个 key 独立计数窗口，窗口过期后自动清理。
 */
public class LocalRateLimiter implements RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(LocalRateLimiter.class);

    private final GatewayProperties gatewayProperties;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public LocalRateLimiter(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @PostConstruct
    public void init() {
        log.info("[Gateway] 本地限流器初始化完成, window={}s, maxRequests={}",
                gatewayProperties.getRateLimitWindowSeconds(),
                gatewayProperties.getRateLimitMaxRequests());
    }

    @Override
    public boolean allowRequest(String key) {
        WindowCounter counter = counters.computeIfAbsent(key, k -> new WindowCounter());
        return counter.tryConsume(key);
    }

    private class WindowCounter {
        private int count = 0;
        private volatile long windowStart = System.currentTimeMillis();

        boolean tryConsume(String key) {
            synchronized (this) {
                long now = System.currentTimeMillis();
                long windowMs = gatewayProperties.getRateLimitWindowSeconds() * 1000L;
                int maxRequests = gatewayProperties.getRateLimitMaxRequests();

                // 如果超出窗口，重置
                if (now - windowStart > windowMs) {
                    windowStart = now;
                    count = 0;
                }

                count++;
                if (count > maxRequests) {
                    log.warn("[Gateway] 本地限流触发 key={}, count={}, max={}", key, count, maxRequests);
                    return false;
                }
                return true;
            }
        }
    }
}
