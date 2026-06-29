package com.helmsail.lightborrow.framework.lock;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.FrameworkException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** 基于 Redisson，支持看门狗自动续期。 */
@Slf4j
public class DistributedLockService {

    private static final String LOCK_KEY_PREFIX = "lock:";
    private static final long DEFAULT_WAIT_MILLIS = 5000;
    /** -1 表示使用 Redisson 看门狗自动续期 */
    private static final long DEFAULT_LEASE_MILLIS = -1;

    private final RedissonClient redissonClient;

    public DistributedLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public <T> T executeWithLock(String key, Supplier<T> task) {
        return executeWithLock(key, DEFAULT_WAIT_MILLIS, task);
    }

    public <T> T executeWithLock(String key, long waitMs, Supplier<T> task) {
        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + key);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitMs, DEFAULT_LEASE_MILLIS, TimeUnit.MILLISECONDS);
            if (!locked) {
                log.warn("[分布式锁] 获取锁超时 key={}, waitMs={}", key, waitMs);
                throw new FrameworkException(ErrorCode.BIZ_ERROR, key);
            }
            log.debug("[分布式锁] 获取成功 key={}", key);
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[分布式锁] 线程中断 key={}", key, e);
            throw new FrameworkException(ErrorCode.BIZ_ERROR, e, key);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[分布式锁] 释放成功 key={}", key);
            }
        }
    }
}
