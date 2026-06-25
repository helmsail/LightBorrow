package com.helmsail.lightborrow.aiinfra.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容聊天补全请求。包含 tools 字段用于 Function Calling。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public record ChatRequest(
        String model,
        List<ChatMessage> messages,
        Double temperature,
        Integer maxTokens,
        Boolean stream,
        List<Map<String, Object>> tools
) {}
