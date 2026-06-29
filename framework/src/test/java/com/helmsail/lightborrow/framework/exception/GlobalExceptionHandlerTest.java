package com.helmsail.lightborrow.framework.exception;

import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.model.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldHandleBusinessException() throws Exception {
        mockMvc.perform(get("/api/test/biz-error"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400100))
                .andExpect(jsonPath("$.msg").value("业务处理失败"));
    }

    @Test
    void shouldHandleValidationException() throws Exception {
        mockMvc.perform(post("/api/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400008));
    }

    @Test
    void shouldHandleMissingParam() throws Exception {
        mockMvc.perform(get("/api/test/required-param"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400008));
    }

    @Test
    void shouldHandleMethodNotSupported() throws Exception {
        mockMvc.perform(post("/api/test/biz-error"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(405));
    }

    @Test
    void shouldHandleUnknownException() throws Exception {
        mockMvc.perform(get("/api/test/unknown-error"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500000))
                .andExpect(jsonPath("$.msg").value("系统内部错误"));
    }

    @Test
    void shouldHandleTypeMismatch() throws Exception {
        mockMvc.perform(get("/api/test/type-mismatch?id=abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400008));
    }

    @RestController
    @RequestMapping("/api/test")
    static class TestController {

        @GetMapping("/biz-error")
        public Result<Void> bizError() {
            throw new BusinessException(ErrorCode.BIZ_ERROR);
        }

        @PostMapping("/validate")
        public Result<Void> validate(@Valid @RequestBody TestRequest request) {
            return Result.success();
        }

        @GetMapping("/required-param")
        public Result<Void> requiredParam(@RequestParam("name") String name) {
            return Result.success();
        }

        @GetMapping("/unknown-error")
        public Result<Void> unknownError() {
            throw new RuntimeException("unexpected");
        }

        @GetMapping("/type-mismatch")
        public Result<Void> typeMismatch(@RequestParam("id") Long id) {
            return Result.success();
        }
    }

    static class TestRequest {
        @NotBlank(message = "名称不能为空")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
