package com.helmsail.lightborrow.framework.config;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.BusinessException;
import com.helmsail.lightborrow.framework.model.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.StringJoiner;

/**
 * 全局异常处理器。统一返回 {@link Result} 格式。
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnMissingBean(GlobalExceptionHandler.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("[业务异常] code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        StringJoiner sj = new StringJoiner("; ");
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            sj.add(fe.getDefaultMessage());
        }
        log.warn("[参数校验] {}", sj);
        return Result.error(ErrorCode.INVALID_PARAMETER.getCode(), "参数校验失败: " + sj);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        StringJoiner sj = new StringJoiner("; ");
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            sj.add(fe.getDefaultMessage());
        }
        log.warn("[参数绑定] {}", sj);
        return Result.error(ErrorCode.INVALID_PARAMETER.getCode(), "参数校验失败: " + sj);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        StringJoiner sj = new StringJoiner("; ");
        for (ConstraintViolation<?> cv : e.getConstraintViolations()) {
            sj.add(cv.getMessage());
        }
        log.warn("[参数校验] {}", sj);
        return Result.error(ErrorCode.INVALID_PARAMETER.getCode(), "参数校验失败: " + sj);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("[参数校验] 缺少必要参数 name={}", e.getParameterName());
        return Result.error(ErrorCode.INVALID_PARAMETER.getCode(), "缺少必要参数: " + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("[参数校验] 参数类型不匹配 name={}, requiredType={}", e.getName(), e.getRequiredType());
        return Result.error(ErrorCode.INVALID_PARAMETER.getCode(), "参数类型错误: " + e.getName());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.error(HttpStatus.METHOD_NOT_ALLOWED.value(), "请求方法不支持: " + e.getMethod());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResource(NoResourceFoundException e) {
        return Result.error(HttpStatus.NOT_FOUND.value(), "资源不存在");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("[参数校验] 请求体格式错误: {}", e.getMessage());
        return Result.error(HttpStatus.BAD_REQUEST.value(), "请求数据格式错误");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<Void> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return Result.error(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "不支持的 Content-Type: " + e.getContentType());
    }

    /** 兜底：打印完整堆栈 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnknown(Exception e) {
        log.error("[系统异常] 未捕获异常", e);
        return Result.error(ErrorCode.SYSTEM_ERROR);
    }
}
