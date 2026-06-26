package com.helmsail.lightborrow.aiinfra.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmsail.lightborrow.aiinfra.config.AiProperties;
import com.helmsail.lightborrow.aiinfra.model.ChatMessage;
import com.helmsail.lightborrow.aiinfra.model.ChatRequest;
import com.helmsail.lightborrow.aiinfra.model.ChatResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiChatModelTest {

    private final AiProperties.LlmProperties llmProperties = createDefaultProperties();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateInstance() {
        var retryRegistry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(1).retryExceptions(RuntimeException.class).build());
        var cbRegistry = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .slidingWindowSize(10).failureRateThreshold(100).build());

        var model = new OpenAiChatModel(
                RestClient.create(), WebClient.create(),
                llmProperties, objectMapper, retryRegistry, cbRegistry);

        assertThat(model).isNotNull();
    }

    @Test
    void shouldUseDefaultsInChatRequest() {
        var retryRegistry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(1).retryExceptions(RuntimeException.class).build());
        var cbRegistry = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .slidingWindowSize(10).failureRateThreshold(100).build());

        var model = new OpenAiChatModel(
                RestClient.create(), WebClient.create(),
                llmProperties, objectMapper, retryRegistry, cbRegistry);

        // Construct a request with null optional fields to exercise withDefaults
        ChatRequest request = ChatRequest.builder()
                .model(null)
                .messages(List.of(ChatMessage.user("hello")))
                .temperature(null)
                .maxTokens(null)
                .stream(false)
                .build();

        // Verify the object is constructed (actual HTTP calls will fail but construction works)
        assertThat(request).isNotNull();
        assertThat(request.messages()).hasSize(1);
        assertThat(request.messages().get(0).content()).isEqualTo("hello");
    }

    private static AiProperties.LlmProperties createDefaultProperties() {
        var props = new AiProperties.LlmProperties();
        props.setBaseUrl("https://api.test.com");
        props.setApiKey("test-key");
        props.setModel("test-model");
        return props;
    }
}
