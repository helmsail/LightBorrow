package com.helmsail.lightborrow.framework.constant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeTest {

    @Test
    void systemError_shouldHaveCorrectValues() {
        assertThat(ErrorCode.SYSTEM_ERROR.getCode()).isEqualTo(500000);
        assertThat(ErrorCode.SYSTEM_ERROR.getMessage()).isEqualTo("系统内部错误");
    }

    @Test
    void invalidParameter_shouldHaveCorrectValues() {
        assertThat(ErrorCode.INVALID_PARAMETER.getCode()).isEqualTo(400008);
        assertThat(ErrorCode.INVALID_PARAMETER.getMessage()).isEqualTo("参数校验失败");
    }

    @Test
    void bizError_shouldHaveCorrectValues() {
        assertThat(ErrorCode.BIZ_ERROR.getCode()).isEqualTo(400100);
        assertThat(ErrorCode.BIZ_ERROR.getMessage()).isEqualTo("业务处理失败");
    }

    @Test
    void getMessageWithArgs_shouldReturnOriginalWhenNoArgs() {
        assertThat(ErrorCode.SYSTEM_ERROR.getMessage()).isEqualTo("系统内部错误");
    }

    @Test
    void getMessageWithArgs_shouldReturnOriginalWhenEmptyArgs() {
        assertThat(ErrorCode.SYSTEM_ERROR.getMessage()).isEqualTo("系统内部错误");
    }

    @Test
    void getMessageWithArgs_shouldFormatWithStringFormat() {
        // ErrorCode messages that don't contain %s placeholders should return original
        assertThat(ErrorCode.RESOURCE_NOT_FOUND.getMessage("user")).isEqualTo("资源不存在");
    }

    @Test
    void description_shouldMatchCodeRange() {
        // Framework 层: 400000-400999
        assertThat(ErrorCode.FRAMEWORK_ERROR.getCode()).isBetween(400000, 400999);
        assertThat(ErrorCode.REDIS_OPERATION_FAILED.getCode()).isBetween(400000, 400999);
        assertThat(ErrorCode.HTTP_REQUEST_FAILED.getCode()).isBetween(400000, 400999);
        assertThat(ErrorCode.LOCK_ACQUISITION_FAILED.getCode()).isBetween(400000, 400999);
        assertThat(ErrorCode.LOCK_ACQUISITION_TIMEOUT.getCode()).isBetween(400000, 400999);
        assertThat(ErrorCode.JSON_SERIALIZE_FAILED.getCode()).isBetween(400000, 400999);
        assertThat(ErrorCode.JSON_DESERIALIZE_FAILED.getCode()).isBetween(400000, 400999);
        assertThat(ErrorCode.ID_GENERATION_FAILED.getCode()).isBetween(400000, 400999);
        assertThat(ErrorCode.INVALID_PARAMETER.getCode()).isBetween(400000, 400999);

        // 业务通用: 400100-400199
        assertThat(ErrorCode.BIZ_ERROR.getCode()).isBetween(400100, 400199);
        assertThat(ErrorCode.RESOURCE_NOT_FOUND.getCode()).isBetween(400100, 400199);
        assertThat(ErrorCode.RESOURCE_ALREADY_EXISTS.getCode()).isBetween(400100, 400199);
        assertThat(ErrorCode.OPERATION_NOT_ALLOWED.getCode()).isBetween(400100, 400199);

        // 系统级: 500000
        assertThat(ErrorCode.SYSTEM_ERROR.getCode()).isEqualTo(500000);
    }
}
