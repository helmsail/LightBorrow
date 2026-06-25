package com.helmsail.lightborrow.framework.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;

/**
 * 框架层内部异常。继承 BusinessException 以便被统一异常处理器捕获。
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
