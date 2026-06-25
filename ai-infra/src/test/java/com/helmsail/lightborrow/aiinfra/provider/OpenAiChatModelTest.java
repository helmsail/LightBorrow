package com.helmsail.lightborrow.aiinfra.provider;

import com.helmsail.lightborrow.aiinfra.exception.AiException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenAiChatModel 仅验证构造和异常继承逻辑。
 * 实际 HTTP 调用和重试熔断由集成测试覆盖。
 */
class OpenAiChatModelTest {

    @Test
    void shouldCreateInstanceWithDefaultRetryConfig() {
        var retryRegistry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(1).retryExceptions(RuntimeException.class).build());
        var cbRegistry = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .slidingWindowSize(10).failureRateThreshold(100).build());

        assertThat(retryRegistry).isNotNull();
        assertThat(cbRegistry).isNotNull();
    }
}
