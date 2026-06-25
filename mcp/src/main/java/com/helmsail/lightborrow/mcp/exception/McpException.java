package com.helmsail.lightborrow.mcp.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.BusinessException;

/**
 * MCP 模块异常。错误码范围 404000-404999。
 */
public class McpException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public McpException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public McpException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}
