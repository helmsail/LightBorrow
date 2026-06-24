package com.helmsail.lightborrow.framework.constant;

import lombok.Getter;

/**
 * 错误码枚举。按模块预留码段 400000-405999, 500000 系统级。
 */

@Getter
public enum ErrorCode {

    // === 系统级 ===
    SYSTEM_ERROR(500000, "系统内部错误"),

    // === Framework 层 (400000-400999) ===
    FRAMEWORK_ERROR(400000, "框架处理异常"),
    REDIS_OPERATION_FAILED(400001, "Redis 操作失败"),
    HTTP_REQUEST_FAILED(400002, "HTTP 请求失败"),
    LOCK_ACQUISITION_FAILED(400003, "获取分布式锁失败"),
    LOCK_ACQUISITION_TIMEOUT(400004, "获取分布式锁超时"),
    JSON_SERIALIZE_FAILED(400005, "JSON 序列化失败"),
    JSON_DESERIALIZE_FAILED(400006, "JSON 反序列化失败"),
    ID_GENERATION_FAILED(400007, "ID 生成失败"),
    INVALID_PARAMETER(400008, "参数校验失败"),

    // === 业务通用 ===
    BIZ_ERROR(400100, "业务处理失败"),
    RESOURCE_NOT_FOUND(400101, "资源不存在"),
    RESOURCE_ALREADY_EXISTS(400102, "资源已存在"),
    OPERATION_NOT_ALLOWED(400103, "操作不允许");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage(Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        return String.format(message, args);
    }
}
