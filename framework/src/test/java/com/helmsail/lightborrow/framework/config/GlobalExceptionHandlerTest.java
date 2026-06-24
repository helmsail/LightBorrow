package com.helmsail.lightborrow.framework.config;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.exception.BusinessException;
import com.helmsail.lightborrow.framework.model.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleBusinessException_shouldReturnErrorJson() throws Exception {
        mockMvc.perform(get("/test/business-error"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400100))
                .andExpect(jsonPath("$.msg").value("业务处理失败"));
    }

    @Test
    void handleValidation_shouldReturnParamError() throws Exception {
        String invalidJson = "{}";
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400008));
    }

    @Test
    void handleMissingParam_shouldReturnError() throws Exception {
        mockMvc.perform(get("/test/missing-param"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400008))
                .andExpect(jsonPath("$.msg").value("缺少必要参数: name"));
    }

    @Test
    void handleNoResource_shouldReturn404() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Result<Void> result = handler.handleNoResource(
                new NoResourceFoundException(HttpMethod.GET, "/non-existent"));
        assertThat(result.getCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void handleMethodNotSupported_shouldReturnError() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Result<Void> result = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("POST", List.of("GET")));
        assertThat(result.getCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
    }

    @Test
    void handleUnknownException_shouldReturnSystemError() throws Exception {
        mockMvc.perform(get("/test/unknown-error"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SYSTEM_ERROR.getCode()))
                .andExpect(jsonPath("$.msg").value(ErrorCode.SYSTEM_ERROR.getMessage()));
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/business-error")
        public Result<Void> businessError() {
            throw new BusinessException(ErrorCode.BIZ_ERROR);
        }

        @PostMapping("/validation")
        public Result<Void> validation(@Valid @RequestBody TestForm form) {
            return Result.success();
        }

        @GetMapping("/missing-param")
        public Result<Void> missingParam(@RequestParam String name) {
            return Result.success();
        }

        @GetMapping("/unknown-error")
        public Result<Void> unknownError() {
            throw new RuntimeException("unexpected");
        }
    }

    @Data
    static class TestForm {
        @NotBlank(message = "名称不能为空")
        private String name;
    }
}
