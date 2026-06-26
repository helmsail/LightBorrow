package com.helmsail.lightborrow.aiinfra.provider;

import com.helmsail.lightborrow.aiinfra.config.AiProperties;
import com.helmsail.lightborrow.aiinfra.config.Resilience4jConfig;
import com.helmsail.lightborrow.aiinfra.embedding.EmbeddingModel;
import com.helmsail.lightborrow.aiinfra.exception.AiException;
import com.helmsail.lightborrow.aiinfra.model.EmbeddingRequest;
import com.helmsail.lightborrow.aiinfra.model.EmbeddingResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.function.Supplier;

import static com.helmsail.lightborrow.framework.constant.ErrorCode.AI_EMBEDDING_FAILED;

/**
 * OpenAI 兼容 Embedding 模型实现（如 DeepSeek Embedding）。
 * 使用 RestClient + Resilience4j 重试熔断。
 */
@Slf4j
public class OpenAiEmbeddingModel implements EmbeddingModel {

    private static final String EMBEDDING_PATH = "/embeddings";

    private final RestClient restClient;
    private final AiProperties.EmbeddingProperties properties;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public OpenAiEmbeddingModel(RestClient restClient,
                                AiProperties.EmbeddingProperties properties,
                                RetryRegistry retryRegistry,
                                CircuitBreakerRegistry cbRegistry) {
        this.restClient = restClient;
        this.properties = properties;
        this.retry = retryRegistry.retry(Resilience4jConfig.RETRY_AI);
        this.circuitBreaker = cbRegistry.circuitBreaker(Resilience4jConfig.CB_AI);
    }

    @Override
    public float[] embed(String text) {
        EmbeddingRequest request = new EmbeddingRequest(properties.getModel(), text);
        Supplier<float[]> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
                Retry.decorateSupplier(retry, () -> doEmbed(request)));
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new AiException(AI_EMBEDDING_FAILED, e, properties.getModel());
        }
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        EmbeddingRequest request = new EmbeddingRequest(properties.getModel(), texts);
        Supplier<List<float[]>> supplier = CircuitBreaker.decorateSupplier(circuitBreaker,
                Retry.decorateSupplier(retry, () -> doEmbedBatch(request)));
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new AiException(AI_EMBEDDING_FAILED, e, properties.getModel());
        }
    }

    private float[] doEmbed(EmbeddingRequest request) {
        log.debug("Calling Embedding: model={}", request.model());
        EmbeddingResponse response = restClient.post()
                .uri(EMBEDDING_PATH)
                .body(request)
                .retrieve()
                .body(EmbeddingResponse.class);
        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new AiException(AI_EMBEDDING_FAILED, "Embedding returned empty response");
        }
        return response.embedding();
    }

    private List<float[]> doEmbedBatch(EmbeddingRequest request) {
        Object input = request.input();
        int size = (input instanceof List<?> list) ? list.size() : 1;
        log.debug("Calling Embedding batch: model={}, size={}", request.model(), size);
        EmbeddingResponse response = restClient.post()
                .uri(EMBEDDING_PATH)
                .body(request)
                .retrieve()
                .body(EmbeddingResponse.class);
        if (response == null || response.data() == null) {
            throw new AiException(AI_EMBEDDING_FAILED, "Embedding batch returned empty response");
        }
        return response.embeddings();
    }
}
