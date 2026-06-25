package com.helmsail.lightborrow.framework.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import lombok.Getter;

/**
 * 业务异常基类。封装 ErrorCode。
 * 各模块继承此基类定义特定异常。
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    /** 快捷构造，默认 BIZ_ERROR */
    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.BIZ_ERROR.getCode();
    }

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessage(args));
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.getMessage(args), cause);
        this.code = errorCode.getCode();
    }
}
