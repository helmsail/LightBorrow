package com.helmsail.lightborrow.aiinfra.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

/**
 * OpenAI 兼容 Embedding 响应。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EmbeddingResponse(
        List<EmbeddingData> data,
        Usage usage
) {

    public float[] embedding() {
        if (data == null || data.isEmpty()) return new float[0];
        return data.get(0).embedding();
    }

    public List<float[]> embeddings() {
        if (data == null) return List.of();
        return data.stream().map(EmbeddingData::embedding).toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EmbeddingData(int index, float[] embedding) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Usage(int promptTokens, int totalTokens) {}
}
