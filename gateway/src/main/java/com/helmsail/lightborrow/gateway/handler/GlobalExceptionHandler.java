package com.helmsail.lightborrow.gateway.handler;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.model.Result;
import com.helmsail.lightborrow.gateway.exception.GatewayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gateway 模块全局异常处理器。
 * 所有未被 Filter 捕获的异常统一转为 {@link Result<T>} 格式返回。
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.helmsail.lightborrow.gateway")
public class GlobalExceptionHandler {

    @ExceptionHandler(GatewayException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleGatewayException(GatewayException e) {
        log.warn("[Gateway] 业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("[Gateway] 参数校验失败: {}", msg);
        return Result.error(ErrorCode.INVALID_PARAMETER.getCode(), msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleUnhandledException(Exception e) {
        log.error("[Gateway] 未捕获异常: {}", e.getMessage(), e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统繁忙，请稍后重试");
    }
}
