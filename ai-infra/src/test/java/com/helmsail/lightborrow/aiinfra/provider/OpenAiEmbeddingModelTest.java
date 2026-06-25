package com.helmsail.lightborrow.aiinfra.provider;

import com.helmsail.lightborrow.aiinfra.exception.AiException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenAiEmbeddingModel 单元测试。
 * 实际 HTTP 调用和重试熔断由集成测试覆盖。
 */
class OpenAiEmbeddingModelTest {

    @Test
    void shouldCreateInstanceWithDefaultConfig() {
        var retryRegistry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(1).retryExceptions(RuntimeException.class).build());
        var cbRegistry = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .slidingWindowSize(10).failureRateThreshold(100).build());

        var properties = new com.helmsail.lightborrow.aiinfra.config.AiProperties.EmbeddingProperties();
        properties.setBaseUrl("https://api.test.com");
        properties.setApiKey("test-key");

        var model = new OpenAiEmbeddingModel(
                org.springframework.web.client.RestClient.create(),
                properties, retryRegistry, cbRegistry);

        assertThat(model).isNotNull();
    }
}
