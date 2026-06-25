package com.helmsail.lightborrow.gateway.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.BusinessException;

/**
 * Gateway 模块异常。
 */
public class GatewayException extends BusinessException {

    public GatewayException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public GatewayException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }
}
