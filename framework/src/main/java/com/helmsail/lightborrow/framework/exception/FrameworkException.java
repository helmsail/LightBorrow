package com.helmsail.lightborrow.framework.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;

/**
 * Framework 层异常。框架内部使用，如 JSON/Redis/锁等基础服务异常。
 */
public class FrameworkException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public FrameworkException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public FrameworkException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}
