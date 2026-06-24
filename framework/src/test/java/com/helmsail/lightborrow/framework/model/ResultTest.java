package com.helmsail.lightborrow.framework.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class ResultTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void success_shouldReturnOk() {
        Result<Void> result = Result.success();
        assertThat(result.getCode()).isEqualTo(Result.SUCCESS_CODE);
        assertThat(result.getMsg()).isEqualTo(Result.SUCCESS_MSG);
        assertThat(result.getData()).isNull();
    }

    @Test
    void successWithData_shouldReturnOk() {
        Result<Integer> result = Result.success(42);
        assertThat(result.getCode()).isEqualTo(Result.SUCCESS_CODE);
        assertThat(result.getMsg()).isEqualTo(Result.SUCCESS_MSG);
        assertThat(result.getData()).isEqualTo(42);
    }

    @Test
    void successWithMsgAndData_shouldReturnOk() {
        Result<String> result = Result.success("自定义消息", "data");
        assertThat(result.getCode()).isEqualTo(Result.SUCCESS_CODE);
        assertThat(result.getMsg()).isEqualTo("自定义消息");
        assertThat(result.getData()).isEqualTo("data");
    }

    @Test
    void successWithMsgOnly_shouldReturnOk() {
        Result<Void> result = Result.success("自定义消息");
        assertThat(result.getCode()).isEqualTo(Result.SUCCESS_CODE);
        assertThat(result.getMsg()).isEqualTo("自定义消息");
        assertThat(result.getData()).isNull();
    }

    @Test
    void errorWithCodeAndMsg_shouldReturnError() {
        Result<Void> result = Result.error(400, "参数错误");
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMsg()).isEqualTo("参数错误");
        assertThat(result.getData()).isNull();
    }

    @Test
    void errorWithErrorCode_shouldReturnError() {
        Result<Void> result = Result.error(ErrorCode.INVALID_PARAMETER);
        assertThat(result.getCode()).isEqualTo(400008);
        assertThat(result.getMsg()).isEqualTo("参数校验失败");
        assertThat(result.getData()).isNull();
    }

    @Test
    void errorWithErrorCodeAndArgs_shouldFormatMessage() {
        Result<Void> result = Result.error(ErrorCode.BIZ_ERROR, "order not found");
        assertThat(result.getCode()).isEqualTo(400100);
        assertThat(result.getMsg()).isEqualTo("业务处理失败");
        assertThat(result.getData()).isNull();
    }

    @Test
    void errorWithBusinessException_shouldExtractCodeAndMessage() {
        BusinessException e = new BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED, "lock:order");
        Result<Void> result = Result.error(e);
        assertThat(result.getCode()).isEqualTo(400003);
        assertThat(result.getMsg()).isEqualTo("获取分布式锁失败");
    }

    @Test
    void jsonSerialization_shouldUseMsgField() throws JsonProcessingException {
        Result<Integer> result = Result.success(42);

        String json = mapper.writeValueAsString(result);

        assertThat(json).contains("\"code\":200");
        assertThat(json).contains("\"msg\":\"操作成功\"");
        assertThat(json).contains("\"data\":42");
    }

    @Test
    void jsonSerialization_shouldExcludeNullData() throws JsonProcessingException {
        Result<Void> result = Result.success();

        String json = mapper.writeValueAsString(result);

        assertThat(json).contains("\"code\":200");
        assertThat(json).contains("\"msg\":\"操作成功\"");
        // data is null, @JsonInclude(NON_NULL) should exclude it
        assertThat(json).doesNotContain("\"data\"");
    }

    @Test
    void jsonDeserialization_shouldMapMsgField() throws JsonProcessingException {
        String json = "{\"code\":200,\"msg\":\"成功\",\"data\":\"test\"}";

        Result<String> result = mapper.readValue(json, Result.class);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMsg()).isEqualTo("成功");
    }

    @Test
    void equalsAndHashCode_shouldWork() {
        Result<Integer> r1 = Result.success(42);
        Result<Integer> r2 = Result.success(42);
        Result<Integer> r3 = Result.success(99);

        assertThat(r1).isEqualTo(r2);
        assertThat(r1).isNotEqualTo(r3);
    }
}
