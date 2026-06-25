package com.helmsail.lightborrow.aiinfra.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 对话消息。OpenAI 兼容格式，支持 system/user/assistant/tool 四种角色。
 * assistant 消息可包含 tool_calls，tool 消息包含 tool_call_id。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatMessage(
        String role,
        String content,
        String name,
        @JsonProperty("tool_call_id") String toolCallId,
        @JsonProperty("tool_calls") List<ToolCall> toolCalls
) {

    public static ChatMessage system(String content) {
        return new ChatMessage("system", content, null, null, null);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content, null, null, null);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content, null, null, null);
    }

    public static ChatMessage assistantWithToolCalls(List<ToolCall> toolCalls) {
        return new ChatMessage("assistant", null, null, null, toolCalls);
    }

    public static ChatMessage tool(String content, String toolCallId, String name) {
        return new ChatMessage("tool", content, name, toolCallId, null);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ToolCall(String id, String type, FunctionCall function) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FunctionCall(String name, String arguments) {}
}
