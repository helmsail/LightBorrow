package com.helmsail.lightborrow.framework.model;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Test
    void shouldReturnSuccessWhenCodeIs200() {
        Result<String> result = Result.success("data");
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo("data");
    }

    @Test
    void shouldReturnSuccessWithoutData() {
        Result<Void> result = Result.success();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMsg()).isEqualTo("操作成功");
        assertThat(result.getData()).isNull();
    }

    @Test
    void shouldReturnSuccessWithCustomMessage() {
        Result<String> result = Result.success("自定义消息", "data");
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMsg()).isEqualTo("自定义消息");
        assertThat(result.getData()).isEqualTo("data");
    }

    @Test
    void shouldReturnErrorWithCodeAndMessage() {
        Result<Void> result = Result.error(400, "错误消息");
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).isEqualTo("错误消息");
        assertThat(result.getData()).isNull();
    }

    @Test
    void shouldReturnErrorFromErrorCode() {
        Result<Void> result = Result.error(ErrorCode.BIZ_ERROR);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400100);
        assertThat(result.getMsg()).isEqualTo("业务处理失败");
    }

    @Test
    void shouldReturnErrorFromErrorCodeWithArgs() {
        Result<Void> result = Result.error(ErrorCode.RESOURCE_NOT_FOUND, "order_123");
        assertThat(result.getCode()).isEqualTo(400101);
        assertThat(result.getMsg()).isEqualTo("资源不存在");
    }

    @Test
    void shouldReturnErrorFromBusinessException() {
        BusinessException be = new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED);
        Result<Void> result = Result.error(be);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400103);
        assertThat(result.getMsg()).isEqualTo("操作不允许");
    }

    @Test
    void shouldNotIncludeNullDataInJson() {
        Result<Void> result = Result.success();
        assertThat(result.getData()).isNull();
    }
}
