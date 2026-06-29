package com.helmsail.lightborrow.memory.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.BusinessException;

/** 错误码范围 406000-406999。 */
public class MemoryException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public MemoryException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public MemoryException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}
