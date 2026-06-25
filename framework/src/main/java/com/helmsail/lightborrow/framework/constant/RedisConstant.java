package com.helmsail.lightborrow.framework.constant;

import java.time.Duration;

/**
 * Redis 相关常量
 */
public final class RedisConstant {

    private RedisConstant() {
    }

    // ========== Key 前缀 ==========
    /** 分布式锁前缀 */
    public static final String LOCK_PREFIX = "lock:";

    // ========== 分布式锁默认配置 ==========
    /** 锁默认持有时间（-1ms 启用 Redisson 看门狗自动续期） */
    public static final Duration LOCK_DEFAULT_LEASE = Duration.ofMillis(-1);
    /** 锁默认等待时间：5 秒 */
    public static final Duration LOCK_DEFAULT_WAIT = Duration.ofSeconds(5);
}
