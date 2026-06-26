package com.helmsail.lightborrow.framework.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Test
    void shouldCreateWithMessageOnly() {
        BusinessException e = new BusinessException("业务异常");
        assertThat(e.getMessage()).isEqualTo("业务异常");
        assertThat(e.getCode()).isEqualTo(ErrorCode.BIZ_ERROR.getCode());
    }

    @Test
    void shouldCreateWithErrorCode() {
        BusinessException e = new BusinessException(ErrorCode.INVALID_PARAMETER);
        assertThat(e.getCode()).isEqualTo(400008);
        assertThat(e.getMessage()).isEqualTo("参数校验失败");
    }

    @Test
    void shouldCreateWithErrorCodeAndArgs() {
        BusinessException e = new BusinessException(ErrorCode.INVALID_PARAMETER, "userId");
        assertThat(e.getCode()).isEqualTo(400008);
        assertThat(e.getMessage()).isEqualTo("参数校验失败");
    }

    @Test
    void shouldCreateWithErrorCodeAndCause() {
        Throwable cause = new RuntimeException("root cause");
        BusinessException e = new BusinessException(ErrorCode.BIZ_ERROR, cause);
        assertThat(e.getCause()).isSameAs(cause);
        assertThat(e.getCode()).isEqualTo(400100);
    }

    @Test
    void shouldCreateWithErrorCodeCauseAndArgs() {
        Throwable cause = new RuntimeException("root");
        BusinessException e = new BusinessException(ErrorCode.BIZ_ERROR, cause, "arg1");
        assertThat(e.getCause()).isSameAs(cause);
        assertThat(e.getCode()).isEqualTo(400100);
    }
}
