package com.helmsail.lightborrow.framework.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AsyncConfigTest {

    private final AsyncConfig asyncConfig = new AsyncConfig();

    @Mock
    private TaskDecorator taskDecorator;

    @Test
    void shouldHaveEnableAsyncAnnotation() {
        assertThat(AsyncConfig.class.isAnnotationPresent(EnableAsync.class)).isTrue();
    }

    @Test
    void taskExecutor_shouldUseConfiguredParams() {
        ThreadPoolTaskExecutor executor = asyncConfig.taskExecutor(taskDecorator);
        executor.initialize();

        ThreadPoolExecutor threadPool = executor.getThreadPoolExecutor();

        assertThat(executor.getThreadNamePrefix()).isEqualTo("async-");
        assertThat(threadPool.getCorePoolSize()).isEqualTo(2);
        assertThat(threadPool.getMaximumPoolSize()).isEqualTo(10);
        assertThat(executor.getQueueCapacity()).isEqualTo(200);
        assertThat(threadPool.getRejectedExecutionHandler())
                .isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);

        executor.destroy();
    }
}
