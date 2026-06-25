package com.helmsail.lightborrow.aiinfra.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmsail.lightborrow.aiinfra.config.AiProperties;
import com.helmsail.lightborrow.aiinfra.config.Resilience4jConfig;
import com.helmsail.lightborrow.aiinfra.exception.AiException;
import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.model.ChatRequest;
import com.helmsail.lightborrow.aiinfra.model.ChatResponse;
import com.helmsail.lightborrow.aiinfra.model.ChatResponseChunk;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Objects;
import java.util.function.Supplier;

import static com.helmsail.lightborrow.framework.constant.ErrorCode.AI_API_CALL_FAILED;

/**
 * OpenAI 兼容聊天模型实现（如 DeepSeek）。
 * 同步调用使用 RestClient + Resilience4j 重试熔断，流式调用使用 WebClient SSE。
 */
@Slf4j
public class OpenAiChatModel implements ChatModel {

    private static final String CHAT_PATH = "/chat/completions";

    private final RestClient restClient;
    private final WebClient webClient;
    private final AiProperties.LlmProperties properties;
    private final ObjectMapper objectMapper;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public OpenAiChatModel(RestClient restClient, WebClient webClient,
                           AiProperties.LlmProperties properties,
                           ObjectMapper objectMapper,
                           RetryRegistry retryRegistry,
                           CircuitBreakerRegistry cbRegistry) {
        this.restClient = restClient;
        this.webClient = webClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.retry = retryRegistry.retry(Resilience4jConfig.RETRY_AI);
        this.circuitBreaker = cbRegistry.circuitBreaker(Resilience4jConfig.CB_AI);
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        ChatRequest finalRequest = withDefaults(request, false);
        Supplier<ChatResponse> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
                Retry.decorateSupplier(retry, () -> doChat(finalRequest)));
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new AiException(AI_API_CALL_FAILED, e, properties.getModel());
        }
    }

    @Override
    public Flux<ChatResponseChunk> stream(ChatRequest request) {
        ChatRequest finalRequest = withDefaults(request, true);
        return doStream(finalRequest);
    }

    private ChatResponse doChat(ChatRequest request) {
        log.debug("Calling LLM chat: model={}", request.model());
        ChatResponse response = restClient.post()
                .uri(CHAT_PATH)
                .body(request)
                .retrieve()
                .body(ChatResponse.class);
        if (response == null) {
            throw new AiException(AI_API_CALL_FAILED, "LLM returned null response");
        }
        return response;
    }

    private Flux<ChatResponseChunk> doStream(ChatRequest request) {
        log.debug("Calling LLM stream: model={}", request.model());
        return webClient.post()
                .uri(CHAT_PATH)
                .bodyValue(request)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> line.startsWith("data: "))
                .map(line -> {
                    String json = line.substring(6).trim();
                    if ("[DONE]".equals(json)) return null;
                    try {
                        return objectMapper.readValue(json, ChatResponseChunk.class);
                    } catch (Exception e) {
                        log.warn("Failed to parse SSE chunk: {}", json, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull);
    }

    private ChatRequest withDefaults(ChatRequest original, boolean stream) {
        AiProperties.ClientOptions opts = properties.getOptions();
        return ChatRequest.builder()
                .model(original.model() != null ? original.model() : properties.getModel())
                .messages(original.messages())
                .temperature(original.temperature() != null ? original.temperature() : opts.getTemperature())
                .maxTokens(original.maxTokens() != null ? original.maxTokens() : opts.getMaxTokens())
                .stream(stream)
                .tools(original.tools())
                .build();
    }
}
