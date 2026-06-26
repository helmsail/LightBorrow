package com.helmsail.lightborrow.framework.constant;

import lombok.Getter;
import org.slf4j.helpers.MessageFormatter;

/**
 * 错误码枚举。码段分配：
 *   framework:  400000-400999
 *   ai-infra:   403000-403999
 *   mcp:        404000-404999
 *   rag:        405000-405999
 *   memory:     406000-406999
 *   core:       407000-407999
 *   gateway:    408000-408999
 */
@Getter
public enum ErrorCode {

    // === 系统级 (500xxx) ===
    SYSTEM_ERROR(500000, "系统内部错误"),

    // === Framework 层 (400000-400999) ===
    JSON_SERIALIZE_FAILED(400005, "JSON 序列化失败"),
    JSON_DESERIALIZE_FAILED(400006, "JSON 反序列化失败"),
    ID_GENERATION_FAILED(400007, "ID 生成失败"),
    INVALID_PARAMETER(400008, "参数校验失败"),

    // === 业务通用 (400100-400999) ===
    BIZ_ERROR(400100, "业务处理失败"),

    // === AI 基础设施 (403000-403999) ===
    AI_API_CALL_FAILED(403000, "AI 服务调用失败"),
    AI_EMBEDDING_FAILED(403002, "向量化失败"),
    AI_VECTOR_SEARCH_FAILED(403003, "向量检索失败"),
    AI_VECTOR_STORE_FAILED(403004, "向量存储失败"),

    // === MCP (404000-404999) ===
    MCP_TOOL_NOT_FOUND(404000, "工具未找到"),
    MCP_TOOL_EXECUTION_FAILED(404001, "工具执行失败"),

    // === RAG (405000-405999) ===
    RAG_RETRIEVAL_FAILED(405002, "获取文档失败"),

    // === Memory (406000-406999) ===
    MEMORY_SESSION_FAILED(406000, "会话加载失败"),
    MEMORY_HISTORY_FAILED(406001, "历史加载失败"),
    MEMORY_PROFILE_FAILED(406002, "画像加载失败"),

    // === Core (407000-407999) ===
    CORE_REACT_MAX_STEPS(407000, "ReAct 循环达到最大步数"),

    // === Gateway (408000-408999) ===
    GATEWAY_RATE_LIMITED(408000, "请求过于频繁"),
    GATEWAY_CHANNEL_ERROR(408001, "渠道适配失败"),
    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取格式化后的错误消息。使用 SLF4J MessageFormatter，{} 占位符，缺少参数时不抛异常。
     */
    public String getMessage(Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        return MessageFormatter.arrayFormat(message, args).getMessage();
    }
}
