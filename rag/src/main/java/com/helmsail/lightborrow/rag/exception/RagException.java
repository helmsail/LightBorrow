package com.helmsail.lightborrow.rag.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.BusinessException;

public class RagException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public RagException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public RagException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}
