package com.helmsail.lightborrow.framework.constant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeTest {

    @Test
    void shouldReturnCorrectCodeAndMessage() {
        assertThat(ErrorCode.SYSTEM_ERROR.getCode()).isEqualTo(500000);
        assertThat(ErrorCode.SYSTEM_ERROR.getMessage()).isEqualTo("系统内部错误");
    }

    @Test
    void shouldReturnFormattedMessageWithArgs() {
        String result = ErrorCode.INVALID_PARAMETER.getMessage("参数不能为空");
        assertThat(result).isEqualTo("参数校验失败");
    }

    @Test
    void shouldReturnOriginalMessageWhenNoArgs() {
        assertThat(ErrorCode.BIZ_ERROR.getMessage()).isEqualTo("业务处理失败");
    }

    @Test
    void shouldReturnOriginalMessageWhenArgsNull() {
        assertThat(ErrorCode.BIZ_ERROR.getMessage((Object[]) null)).isEqualTo("业务处理失败");
    }

    @Test
    void shouldHandleMultiplePlaceholders() {
        String result = ErrorCode.RESOURCE_NOT_FOUND.getMessage("order_123");
        assertThat(result).isEqualTo("资源不存在");
    }

    @Test
    void frameworkErrorCodesShouldBeInCorrectRange() {
        assertThat(ErrorCode.FRAMEWORK_ERROR.getCode()).isBetween(400000, 400999);
        assertThat(ErrorCode.ID_GENERATION_FAILED.getCode()).isBetween(400000, 400999);
    }

    @Test
    void allErrorCodesShouldHaveNonEmptyMessage() {
        for (ErrorCode ec : ErrorCode.values()) {
            assertThat(ec.getMessage()).as("ErrorCode %s message should not be empty", ec.name())
                    .isNotBlank();
        }
    }
}
