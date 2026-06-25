package com.helmsail.lightborrow.gateway.model;

/**
 * 回复消息类型，与 AgentResult.type 对齐。
 */
public enum ReplyType {

    /** 最终回复 */
    FINAL,

    /** 追问问题 */
    QUESTION,

    /** 确认请求 */
    CONFIRM,

    /** 中间过程消息 */
    INTERMEDIATE
}
