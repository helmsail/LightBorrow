package com.helmsail.lightborrow.aiinfra.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

/**
 * OpenAI 兼容聊天补全响应。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ChatResponse(
        List<Choice> choices,
        Usage usage
) {

    public String content() {
        if (choices == null || choices.isEmpty()) return null;
        Message msg = choices.get(0).message();
        return msg != null ? msg.content() : null;
    }

    public String finishReason() {
        if (choices == null || choices.isEmpty()) return null;
        return choices.get(0).finishReason();
    }

    public boolean hasToolCalls() {
        List<ChatMessage.ToolCall> calls = toolCalls();
        return calls != null && !calls.isEmpty();
    }

    public List<ChatMessage.ToolCall> toolCalls() {
        if (choices == null || choices.isEmpty()) return null;
        Message msg = choices.get(0).message();
        return msg != null ? msg.toolCalls() : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Choice(Message message, String finishReason) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(String content, @JsonProperty("tool_calls") List<ChatMessage.ToolCall> toolCalls) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Usage(int promptTokens, int completionTokens, int totalTokens) {}
}
