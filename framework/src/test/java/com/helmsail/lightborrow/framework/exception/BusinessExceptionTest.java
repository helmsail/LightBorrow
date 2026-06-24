package com.helmsail.lightborrow.framework.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Test
    void constructorWithMessage_shouldUseBizErrorCode() {
        BusinessException e = new BusinessException("业务异常");
        assertThat(e.getCode()).isEqualTo(ErrorCode.BIZ_ERROR.getCode());
        assertThat(e.getMessage()).isEqualTo("业务异常");
    }

    @Test
    void constructorWithErrorCode_shouldSetCodeAndMessage() {
        BusinessException e = new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        assertThat(e.getCode()).isEqualTo(400101);
        assertThat(e.getMessage()).isEqualTo("资源不存在");
    }

    @Test
    void constructorWithErrorCodeAndArgs_shouldFormatMessage() {
        BusinessException e = new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "order");
        assertThat(e.getCode()).isEqualTo(400101);
        assertThat(e.getMessage()).isEqualTo("资源不存在");
    }

    @Test
    void constructorWithErrorCodeAndCause_shouldWrapCause() {
        Throwable cause = new RuntimeException("db error");
        BusinessException e = new BusinessException(ErrorCode.SYSTEM_ERROR, cause);
        assertThat(e.getCause()).isSameAs(cause);
        assertThat(e.getCode()).isEqualTo(500000);
    }

    @Test
    void constructorWithErrorCodeCauseAndArgs_shouldSetAll() {
        Throwable cause = new RuntimeException("timeout");
        BusinessException e = new BusinessException(ErrorCode.LOCK_ACQUISITION_TIMEOUT, cause, "lock:order");
        assertThat(e.getCause()).isSameAs(cause);
        assertThat(e.getCode()).isEqualTo(400004);
        assertThat(e.getMessage()).isEqualTo("获取分布式锁超时");
    }

    @Test
    void shouldBeRuntimeException() {
        BusinessException e = new BusinessException("test");
        assertThat(e).isInstanceOf(RuntimeException.class);
    }
}
