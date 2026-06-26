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
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistributedLockServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    @InjectMocks
    private DistributedLockService lockService;

    @Test
    void shouldExecuteTaskWhenLockAcquired() throws Exception {
        when(redissonClient.getLock("lock:test-key")).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        Supplier<String> task = () -> "result";
        String result = lockService.executeWithLock("test-key", task);

        assertThat(result).isEqualTo("result");
        verify(lock).unlock();
    }

    @Test
    void shouldThrowWhenLockTimeout() throws Exception {
        when(redissonClient.getLock("lock:test-key")).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        assertThatThrownBy(() -> lockService.executeWithLock("test-key", () -> "result"))
                .isInstanceOf(FrameworkException.class)
                .hasMessageContaining("业务处理失败");

        verify(lock, never()).unlock();
    }

    @Test
    void shouldHandleInterruptedException() throws Exception {
        when(redissonClient.getLock("lock:test-key")).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class)))
                .thenThrow(new InterruptedException("interrupted"));

        AtomicBoolean interrupted = new AtomicBoolean(false);
        Thread.currentThread().interrupt();

        assertThatThrownBy(() -> lockService.executeWithLock("test-key", () -> "result"))
                .isInstanceOf(FrameworkException.class)
                .hasMessageContaining("业务处理失败");

        // Verify interrupt flag is preserved
        assertThat(Thread.interrupted()).isTrue();
    }

    @Test
    void shouldUseCustomWaitMs() throws Exception {
        when(redissonClient.getLock("lock:custom-key")).thenReturn(lock);
        when(lock.tryLock(1000, -1, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        lockService.executeWithLock("custom-key", 1000, () -> "done");

        verify(lock).tryLock(1000, -1, TimeUnit.MILLISECONDS);
    }
}
