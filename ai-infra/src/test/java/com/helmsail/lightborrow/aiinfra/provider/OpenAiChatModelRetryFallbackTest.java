package com.helmsail.lightborrow.aiinfra.provider;

import com.helmsail.lightborrow.aiinfra.config.Resilience4jConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiChatModelRetryFallbackTest {

    @Test
    void shouldConfigureRetryWithCorrectAttempts() {
        var retryRegistry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(10))
                .retryExceptions(RuntimeException.class)
                .build());

        var retry = retryRegistry.retry("test");
        assertThat(retry.getRetryConfig().getMaxAttempts()).isEqualTo(3);
    }

    @Test
    void shouldConfigureCircuitBreakerWithCorrectThreshold() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();

        assertThat(config.getFailureRateThreshold()).isEqualTo(50f);
        assertThat(config.getSlidingWindowSize()).isEqualTo(10);
        assertThat(config.getMinimumNumberOfCalls()).isEqualTo(5);
    }

    @Test
    void resilience4jConfigShouldCreateRetryRegistry() {
        Resilience4jConfig config = new Resilience4jConfig();
        RetryRegistry retryRegistry = config.aiRetryRegistry();

        Retry retry = retryRegistry.retry(Resilience4jConfig.RETRY_AI);
        assertThat(retry.getRetryConfig().getMaxAttempts()).isEqualTo(3);
    }

    @Test
    void resilience4jConfigShouldCreateCircuitBreakerRegistry() {
        Resilience4jConfig config = new Resilience4jConfig();
        CircuitBreakerRegistry cbRegistry = config.aiCircuitBreakerRegistry();

        CircuitBreaker cb = cbRegistry.circuitBreaker(Resilience4jConfig.CB_AI);
        assertThat(cb.getCircuitBreakerConfig().getFailureRateThreshold()).isEqualTo(50f);
        assertThat(cb.getCircuitBreakerConfig().getSlidingWindowSize()).isEqualTo(10);
    }
}
