package com.helmsail.lightborrow.aiinfra.provider;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.helmsail.lightborrow.aiinfra.config.AiProperties;
import com.helmsail.lightborrow.aiinfra.embedding.EmbeddingModel;
import com.helmsail.lightborrow.aiinfra.exception.AiException;
import com.helmsail.lightborrow.aiinfra.model.EmbeddingRequest;
import com.helmsail.lightborrow.aiinfra.model.EmbeddingResponse;
import com.helmsail.lightborrow.framework.sentinel.SentinelAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

import java.util.List;

import static com.helmsail.lightborrow.framework.constant.ErrorCode.AI_EMBEDDING_FAILED;

/**
 * OpenAI 兼容 Embedding 模型实现（如 BGE-M3、Qwen-Embedding）。
 * 使用 RestClient + Sentinel 熔断。
 */
@Slf4j
public class OpenAiEmbeddingModel implements EmbeddingModel {

    private static final String EMBEDDING_PATH = "/embeddings";

    private final RestClient restClient;
    private final AiProperties.EmbeddingProperties properties;

    public OpenAiEmbeddingModel(RestClient restClient,
                                AiProperties.EmbeddingProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public float[] embed(String text) {
        EmbeddingRequest request = new EmbeddingRequest(properties.getModel(), text);
        try (Entry entry = SphU.entry(SentinelAutoConfiguration.RESOURCE_EMBEDDING)) {
            return doEmbed(request);
        } catch (Exception e) {
            throw new AiException(AI_EMBEDDING_FAILED, e, properties.getModel());
        }
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        EmbeddingRequest request = new EmbeddingRequest(properties.getModel(), texts);
        try (Entry entry = SphU.entry(SentinelAutoConfiguration.RESOURCE_EMBEDDING)) {
            return doEmbedBatch(request);
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
