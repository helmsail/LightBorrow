package com.helmsail.lightborrow.gateway.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayExceptionTest {

    @Test
    void shouldCreateWithErrorCode() {
        GatewayException e = new GatewayException(ErrorCode.GATEWAY_CHANNEL_ERROR, "渠道异常");
        assertThat(e.getCode()).isEqualTo(ErrorCode.GATEWAY_CHANNEL_ERROR.getCode());
        assertThat(e.getMessage()).isEqualTo(ErrorCode.GATEWAY_CHANNEL_ERROR.getMessage());
    }

    @Test
    void shouldCreateWithErrorCodeAndCause() {
        Throwable cause = new RuntimeException("network error");
        GatewayException e = new GatewayException(ErrorCode.GATEWAY_RATE_LIMITED, cause);
        assertThat(e.getCause()).isSameAs(cause);
        assertThat(e.getCode()).isEqualTo(ErrorCode.GATEWAY_RATE_LIMITED.getCode());
    }
}
