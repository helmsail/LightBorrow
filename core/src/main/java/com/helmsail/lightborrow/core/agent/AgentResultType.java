package com.helmsail.lightborrow.core.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Agent 处理结果类型。{@link JsonProperty} 确保序列化为前端期望的小写字符串。
 */
public enum AgentResultType {

    @JsonProperty("final") FINAL_ANSWER,
    @JsonProperty("question") QUESTION,
    @JsonProperty("confirm") CONFIRM,
    @JsonProperty("error") ERROR
}
