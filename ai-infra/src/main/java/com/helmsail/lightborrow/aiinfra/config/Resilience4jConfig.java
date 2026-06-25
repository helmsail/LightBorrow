package com.helmsail.lightborrow.aiinfra.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j 重试与熔断配置。为 AI API 调用提供容错能力。
 */
@Configuration(proxyBeanMethods = false)
public class Resilience4jConfig {

    /** AI 重试名称 */
    public static final String RETRY_AI = "ai-retry";

    /** AI 熔断器名称 */
    public static final String CB_AI = "ai-circuitbreaker";

    /**
     * AI 重试策略：最多 3 次，间隔 1s，指数退避 2x。
     */
    @Bean
    public RetryRegistry aiRetryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .retryExceptions(RuntimeException.class)
                .build();
        return RetryRegistry.of(config);
    }

    /**
     * AI 熔断策略：10s 窗口内 50% 失败则熔断，半开后 5s 尝试恢复。
     */
    @Bean
    public CircuitBreakerRegistry aiCircuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
        return CircuitBreakerRegistry.of(config);
    }
}
