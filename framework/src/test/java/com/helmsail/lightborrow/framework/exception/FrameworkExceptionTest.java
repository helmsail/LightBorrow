package com.helmsail.lightborrow.framework.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrameworkExceptionTest {

    @Test
    void shouldCreateWithErrorCode() {
        FrameworkException e = new FrameworkException(ErrorCode.JSON_SERIALIZE_FAILED);
        assertThat(e.getCode()).isEqualTo(400005);
        assertThat(e.getMessage()).isEqualTo("JSON 序列化失败");
    }

    @Test
    void shouldCreateWithErrorCodeAndArgs() {
        FrameworkException e = new FrameworkException(ErrorCode.JSON_SERIALIZE_FAILED, "order");
        assertThat(e.getCode()).isEqualTo(400005);
    }

    @Test
    void shouldCreateWithErrorCodeAndCause() {
        Throwable cause = new RuntimeException("connection refused");
        FrameworkException e = new FrameworkException(ErrorCode.JSON_SERIALIZE_FAILED, cause);
        assertThat(e.getCause()).isSameAs(cause);
    }

    @Test
    void shouldBeInstanceOfBusinessException() {
        FrameworkException e = new FrameworkException(ErrorCode.JSON_SERIALIZE_FAILED);
        assertThat(e).isInstanceOf(BusinessException.class);
    }
}
