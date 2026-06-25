package com.helmsail.lightborrow.aiinfra.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

/**
 * OpenAI 兼容 Embedding 请求。
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EmbeddingRequest(
        String model,
        Object input  // String or List<String>
) {

    public EmbeddingRequest(String model, String input) {
        this(model, (Object) input);
    }

    public EmbeddingRequest(String model, List<String> input) {
        this(model, (Object) input);
    }
}
