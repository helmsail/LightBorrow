package com.helmsail.lightborrow.core.agent;

import lombok.extern.slf4j.Slf4j;

/**
 * LLM Token 用量追踪器。记录每次 Agent 处理的 token 消耗。
 */
@Slf4j
public class LlmUsageTracker {

    private static final String USAGE_LOGGER_NAME = "LlmUsage";

    /**
     * 记录一次 LLM 调用用量。
     */
    public static void record(String userId, String model, int promptTokens,
                              int completionTokens, int totalTokens, long durationMs) {
        log.info("[{}] userId={}, model={}, promptTokens={}, completionTokens={}, "
                        + "totalTokens={}, durationMs={}",
                USAGE_LOGGER_NAME, userId, model,
                promptTokens, completionTokens, totalTokens, durationMs);
    }

    /**
     * 记录带 session 的调用用量。
     */
    public static void record(String userId, String sessionId, String model,
                              int promptTokens, int completionTokens,
                              int totalTokens, long durationMs) {
        log.info("[{}] userId={}, sessionId={}, model={}, promptTokens={}, completionTokens={}, "
                        + "totalTokens={}, durationMs={}",
                USAGE_LOGGER_NAME, userId, sessionId, model,
                promptTokens, completionTokens, totalTokens, durationMs);
    }
}
