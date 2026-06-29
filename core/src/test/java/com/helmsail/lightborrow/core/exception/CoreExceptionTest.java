package com.helmsail.lightborrow.core.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoreExceptionTest {

    @Test
    void shouldCreateWithErrorCode() {
        CoreException ex = new CoreException(ErrorCode.CORE_REACT_MAX_STEPS, "user1");
        assertThat(ex).isInstanceOf(BusinessException.class);
        assertThat(ex.getCode()).isEqualTo(ErrorCode.CORE_REACT_MAX_STEPS.getCode());
    }

    @Test
    void shouldCreateWithErrorCodeAndCause() {
        Throwable cause = new RuntimeException("network error");
        CoreException ex = new CoreException(ErrorCode.CORE_REACT_MAX_STEPS, cause, "user1");
        assertThat(ex.getCause()).isSameAs(cause);
        assertThat(ex.getCode()).isEqualTo(ErrorCode.CORE_REACT_MAX_STEPS.getCode());
    }

    @Test
    void shouldHaveSerialVersionUid() {
        CoreException ex = new CoreException(ErrorCode.CORE_REACT_MAX_STEPS, "user1");
        assertThat(ex).isInstanceOf(BusinessException.class);
    }
}
