package com.helmsail.lightborrow.framework.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrameworkExceptionTest {

    @Test
    void constructorWithErrorCode_shouldSetCodeAndMessage() {
        FrameworkException e = new FrameworkException(ErrorCode.REDIS_OPERATION_FAILED);
        assertThat(e.getCode()).isEqualTo(400001);
        assertThat(e.getMessage()).isEqualTo("Redis 操作失败");
    }

    @Test
    void constructorWithErrorCodeAndArgs_shouldFormatMessage() {
        FrameworkException e = new FrameworkException(ErrorCode.JSON_SERIALIZE_FAILED, "User");
        assertThat(e.getCode()).isEqualTo(400005);
        assertThat(e.getMessage()).isEqualTo("JSON 序列化失败");
    }

    @Test
    void constructorWithErrorCodeAndCause_shouldWrapCause() {
        Throwable cause = new RuntimeException("inner");
        FrameworkException e = new FrameworkException(ErrorCode.LOCK_ACQUISITION_FAILED, cause, "lock:order");
        assertThat(e.getCause()).isSameAs(cause);
        assertThat(e.getCode()).isEqualTo(400003);
        assertThat(e.getMessage()).isEqualTo("获取分布式锁失败");
    }

    @Test
    void shouldExtendBusinessException() {
        FrameworkException e = new FrameworkException(ErrorCode.FRAMEWORK_ERROR);
        assertThat(e).isInstanceOf(BusinessException.class);
    }
}
