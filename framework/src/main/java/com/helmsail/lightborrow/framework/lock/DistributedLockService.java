package com.helmsail.lightborrow.framework.lock;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.constant.RedisConstant;
import com.helmsail.lightborrow.framework.exception.FrameworkException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务。默认 leaseTime=-1ms 启用看门狗自动续期。
 */
@Slf4j
@RequiredArgsConstructor
public class DistributedLockService {

    private final RedissonClient redissonClient;

    public RLock getLock(String name) {
        return redissonClient.getLock(buildLockKey(name));
    }

    /**
     * @param leaseTime 锁持有时间（-1ms 启用看门狗自动续期）
     */
    public <T> T executeWithLock(String name, Duration waitTime, Duration leaseTime,
                                  Supplier<T> supplier) {
        RLock lock = getLock(name);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new FrameworkException(ErrorCode.LOCK_ACQUISITION_TIMEOUT, name);
            }
            log.debug("[分布式锁] 获取成功 lock={}", name);
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FrameworkException(ErrorCode.LOCK_ACQUISITION_FAILED, e, name);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[分布式锁] 释放成功 lock={}", name);
            }
        }
    }

    /** 无返回值重载 */
    public void executeWithLock(String name, Duration waitTime, Duration leaseTime,
                                 Runnable runnable) {
        executeWithLock(name, waitTime, leaseTime, () -> {
            runnable.run();
            return null;
        });
    }

    /** 默认配置（wait=5s, lease=-1ms 看门狗） */
    public <T> T executeWithLock(String name, Supplier<T> supplier) {
        return executeWithLock(name,
                RedisConstant.LOCK_DEFAULT_WAIT,
                RedisConstant.LOCK_DEFAULT_LEASE,
                supplier);
    }

    /** 无返回值重载，使用默认配置 */
    public void executeWithLock(String name, Runnable runnable) {
        executeWithLock(name, () -> {
            runnable.run();
            return null;
        });
    }

    private String buildLockKey(String name) {
        return RedisConstant.LOCK_PREFIX + name;
    }
}
