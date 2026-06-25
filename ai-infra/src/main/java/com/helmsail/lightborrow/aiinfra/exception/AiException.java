package com.helmsail.lightborrow.aiinfra.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.BusinessException;

/**
 * AI 基础设施异常。错误码范围 403000-403999。
 */
public class AiException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public AiException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public AiException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}
