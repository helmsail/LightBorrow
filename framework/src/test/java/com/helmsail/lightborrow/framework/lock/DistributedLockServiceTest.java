package com.helmsail.lightborrow.framework.lock;

import com.helmsail.lightborrow.framework.exception.FrameworkException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class DistributedLockServiceTest {

    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock rLock;

    @InjectMocks
    private DistributedLockService lockService;

    /** tryLock mock - suppressed checked exception since it's a mock */
    private void mockTryLock(boolean result) {
        try { doReturn(result).when(rLock).tryLock(anyLong(), anyLong(), any(TimeUnit.class)); }
        catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    private void mockTryLockThrows(InterruptedException e) {
        try { doThrow(e).when(rLock).tryLock(anyLong(), anyLong(), any(TimeUnit.class)); }
        catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    private void mockIsHeldByCurrentThread() {
        doReturn(true).when(rLock).isHeldByCurrentThread();
    }

    @Test
    void getLock_shouldReturnRedissonLock() {
        when(redissonClient.getLock("lock:order")).thenReturn(rLock);
        assertThat(lockService.getLock("order")).isSameAs(rLock);
    }

    @Test
    void executeWithLock_shouldExecuteSupplierWhenLockAcquired() {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        mockTryLock(true);
        mockIsHeldByCurrentThread();

        String result = lockService.executeWithLock("order", 1000, -1, TimeUnit.MILLISECONDS, () -> "done");

        assertThat(result).isEqualTo("done");
        verify(rLock).unlock();
    }

    @Test
    void executeWithLock_shouldThrowWhenLockNotAcquired() {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        mockTryLock(false);

        assertThatThrownBy(() ->
                lockService.executeWithLock("order", 1000, -1, TimeUnit.MILLISECONDS, () -> "done"))
                .isInstanceOf(FrameworkException.class)
                .hasMessageContaining("获取分布式锁超时");

        verify(rLock, never()).unlock();
    }

    @Test
    void executeWithLock_shouldThrowWhenInterrupted() {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        mockTryLockThrows(new InterruptedException("interrupted"));

        assertThatThrownBy(() ->
                lockService.executeWithLock("order", 1000, -1, TimeUnit.MILLISECONDS, () -> "done"))
                .isInstanceOf(FrameworkException.class)
                .hasMessageContaining("获取分布式锁失败");

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        Thread.interrupted();
    }

    @Test
    void executeWithLockRunnable_shouldExecuteWhenLockAcquired() {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        mockTryLock(true);
        mockIsHeldByCurrentThread();

        AtomicBoolean executed = new AtomicBoolean(false);
        lockService.executeWithLock("order", 1000, -1, TimeUnit.MILLISECONDS, () -> executed.set(true));

        assertThat(executed).isTrue();
        verify(rLock).unlock();
    }

    @Test
    void executeWithLockDefault_shouldUseDefaultTimeout() {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        mockTryLock(true);
        mockIsHeldByCurrentThread();

        String result = lockService.executeWithLock("order", () -> "done");

        assertThat(result).isEqualTo("done");
        verifyTryLock(5000, -1L);
        verify(rLock).unlock();
    }

    private void verifyTryLock(long waitTime, long leaseTime) {
        try { verify(rLock).tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS); }
        catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    @Test
    void executeWithLockRunnableDefault_shouldUseDefaultTimeout() {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        mockTryLock(true);
        mockIsHeldByCurrentThread();

        AtomicBoolean executed = new AtomicBoolean(false);
        lockService.executeWithLock("order", () -> executed.set(true));

        assertThat(executed).isTrue();
        verify(rLock).unlock();
    }

    @Test
    void executeWithLock_shouldReleaseLockInFinally() {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        mockTryLock(true);
        mockIsHeldByCurrentThread();

        assertThatThrownBy(() ->
                lockService.executeWithLock("order", 1000, -1, TimeUnit.MILLISECONDS, () -> {
                    throw new RuntimeException("business error");
                }))
                .isInstanceOf(RuntimeException.class);

        verify(rLock).unlock();
    }
}
