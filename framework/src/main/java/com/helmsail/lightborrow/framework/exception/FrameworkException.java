package com.helmsail.lightborrow.framework.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;

public class FrameworkException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public FrameworkException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public FrameworkException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}
