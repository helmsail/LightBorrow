package com.helmsail.lightborrow.framework.ratelimit;

/**
 * 限流器接口。抽象限流能力，支持 Redis 分布式实现和本地内存实现。
 */
public interface RateLimiter {

    /**
     * 检查指定 key 的请求是否允许通过。
     *
     * @param key 限流 key（如 userId、IP）
     * @return true 允许通过，false 被限流拒绝
     */
    boolean allowRequest(String key);
}
