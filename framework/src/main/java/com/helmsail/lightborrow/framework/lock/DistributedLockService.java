package com.helmsail.lightborrow.framework.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务。基于 Redisson，支持自动续期（看门狗）。
 */
@Slf4j
@RequiredArgsConstructor
public class DistributedLockService {

    private static final String LOCK_KEY_PREFIX = "lock:";
    private static final long DEFAULT_WAIT_MILLIS = 5000;
    /** -1 表示使用 Redisson 看门狗自动续期 */
    private static final long DEFAULT_LEASE_MILLIS = -1;

    private final RedissonClient redissonClient;

    /**
     * 执行带锁的任务。
     *
     * @param key      锁的 key
     * @param task     待执行任务
     * @param <T>      返回值类型
     * @return 任务执行结果
     */
    public <T> T executeWithLock(String key, Supplier<T> task) {
        return executeWithLock(key, DEFAULT_WAIT_MILLIS, task);
    }

    /**
     * 执行带锁的任务。
     *
     * @param key       锁的 key
     * @param waitMs    等待锁的超时时间（毫秒）
     * @param task      待执行任务
     * @param <T>       返回值类型
     * @return 任务执行结果
     */
    public <T> T executeWithLock(String key, long waitMs, Supplier<T> task) {
        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + key);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitMs, DEFAULT_LEASE_MILLIS, TimeUnit.MILLISECONDS);
            if (!locked) {
                log.warn("[分布式锁] 获取锁超时 key={}, waitMs={}", key, waitMs);
                throw new RuntimeException("获取分布式锁超时: " + key);
            }
            log.debug("[分布式锁] 获取成功 key={}", key);
            return task.get();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("[分布式锁] 执行异常 key={}", key, e);
            throw new RuntimeException("分布式锁执行异常: " + key, e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[分布式锁] 释放成功 key={}", key);
            }
        }
    }
}
