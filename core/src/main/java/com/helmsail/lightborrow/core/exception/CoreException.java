package com.helmsail.lightborrow.core.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.BusinessException;

public class CoreException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public CoreException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public CoreException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}
