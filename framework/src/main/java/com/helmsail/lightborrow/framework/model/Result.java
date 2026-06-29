package com.helmsail.lightborrow.framework.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.BusinessException;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/** 统一 API 返回体 {code, msg, data}。*/
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int SUCCESS_CODE = 200;
    public static final String SUCCESS_MSG = "操作成功";

    private int code;
    private String msg;
    private T data;

    private Result() {
    }

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public boolean isSuccess() {
        return code == SUCCESS_CODE;
    }

    // ========== 成功 ==========

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, SUCCESS_MSG, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, SUCCESS_MSG, data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(SUCCESS_CODE, msg, data);
    }

    // ========== 失败 ==========

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> Result<T> error(ErrorCode errorCode, Object... args) {
        return new Result<>(errorCode.getCode(), errorCode.formatMessage(args), null);
    }

    public static <T> Result<T> error(BusinessException e) {
        return new Result<>(e.getCode(), e.getMessage(), null);
    }
}
