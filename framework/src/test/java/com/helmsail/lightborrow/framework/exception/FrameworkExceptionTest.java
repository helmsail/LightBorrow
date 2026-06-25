package com.helmsail.lightborrow.framework.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrameworkExceptionTest {

    @Test
    void shouldCreateWithErrorCode() {
        FrameworkException e = new FrameworkException(ErrorCode.REDIS_OPERATION_FAILED);
        assertThat(e.getCode()).isEqualTo(400001);
        assertThat(e.getMessage()).isEqualTo("Redis 操作失败");
    }

    @Test
    void shouldCreateWithErrorCodeAndArgs() {
        FrameworkException e = new FrameworkException(ErrorCode.JSON_SERIALIZE_FAILED, "order");
        assertThat(e.getCode()).isEqualTo(400005);
    }

    @Test
    void shouldCreateWithErrorCodeAndCause() {
        Throwable cause = new RuntimeException("connection refused");
        FrameworkException e = new FrameworkException(ErrorCode.REDIS_OPERATION_FAILED, cause);
        assertThat(e.getCause()).isSameAs(cause);
    }

    @Test
    void shouldBeInstanceOfBusinessException() {
        FrameworkException e = new FrameworkException(ErrorCode.FRAMEWORK_ERROR);
        assertThat(e).isInstanceOf(BusinessException.class);
    }
}
