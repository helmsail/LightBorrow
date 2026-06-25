package com.helmsail.lightborrow.aiinfra.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

/**
 * OpenAI 兼容流式响应块 (SSE Chat Completion Chunk)。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ChatResponseChunk(
        List<ChunkChoice> choices
) {

    public String content() {
        if (choices == null || choices.isEmpty()) return null;
        Delta delta = choices.get(0).delta();
        return delta != null ? delta.content() : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ChunkChoice(Delta delta, int index, String finishReason) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Delta(String role, String content) {}
}
