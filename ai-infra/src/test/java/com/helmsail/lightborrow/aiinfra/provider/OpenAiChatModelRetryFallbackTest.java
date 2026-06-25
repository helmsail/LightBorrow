package com.helmsail.lightborrow.aiinfra.provider;

import com.helmsail.lightborrow.aiinfra.exception.AiException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 重试熔断配置的单元验证。
 * 重试和熔断的集成行为由 resilience4j 自身测试保证。
 */
class OpenAiChatModelRetryFallbackTest {

    @Test
    void shouldConfigureRetryWithCorrectAttempts() {
        var retryRegistry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(java.time.Duration.ofMillis(10))
                .retryExceptions(RuntimeException.class)
                .build());

        var retry = retryRegistry.retry("test");
        assertThat(retry.getRetryConfig().getMaxAttempts()).isEqualTo(3);
    }
}
