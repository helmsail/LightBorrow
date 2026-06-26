package com.helmsail.lightborrow;

import com.helmsail.lightborrow.core.agent.AgentLoop;
import com.helmsail.lightborrow.core.agent.AgentResult;
import com.helmsail.lightborrow.framework.ratelimit.RateLimiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.helmsail.lightborrow.controller.ChatController;
import com.helmsail.lightborrow.controller.ChatController.ChatRequest;
import com.helmsail.lightborrow.framework.model.Result;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private AgentLoop agentLoop;

    @Mock
    private RateLimiter rateLimiter;

    @InjectMocks
    private ChatController controller;

    // ========== 正常消息流程 ==========

    @Test
    void chat_shouldReturnFinalAnswer() {
        when(rateLimiter.allowRequest(anyString())).thenReturn(true);
        when(agentLoop.process(anyString(), anyString()))
                .thenReturn(AgentResult.finalAnswer("你已成功借出 MacBook Pro"));

        ChatRequest request = new ChatRequest("user1", "借一台 MacBook");
        Result<ChatController.ChatResponse> result = controller.chat(request);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().type()).isEqualTo("final");
        assertThat(result.getData().content()).isEqualTo("你已成功借出 MacBook Pro");
    }

    @Test
    void chat_shouldReturnQuestion() {
        when(rateLimiter.allowRequest(anyString())).thenReturn(true);
        when(agentLoop.process(anyString(), anyString()))
                .thenReturn(AgentResult.question("你想借用什么设备？"));

        ChatRequest request = new ChatRequest("user1", "我想借用设备");
        Result<ChatController.ChatResponse> result = controller.chat(request);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().type()).isEqualTo("question");
        assertThat(result.getData().content()).isEqualTo("你想借用什么设备？");
    }

    @Test
    void chat_shouldReturnConfirm() {
        when(rateLimiter.allowRequest(anyString())).thenReturn(true);
        when(agentLoop.process(anyString(), anyString()))
                .thenReturn(AgentResult.confirm("确认借用 MacBook Pro？"));

        ChatRequest request = new ChatRequest("user1", "确认");
        Result<ChatController.ChatResponse> result = controller.chat(request);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().type()).isEqualTo("confirm");
        assertThat(result.getData().content()).isEqualTo("确认借用 MacBook Pro？");
    }

    @Test
    void chat_shouldReturnError() {
        when(rateLimiter.allowRequest(anyString())).thenReturn(true);
        when(agentLoop.process(anyString(), anyString()))
                .thenReturn(AgentResult.error("系统繁忙"));

        ChatRequest request = new ChatRequest("user1", "test");
        Result<ChatController.ChatResponse> result = controller.chat(request);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().type()).isEqualTo("error");
        assertThat(result.getData().content()).isEqualTo("系统繁忙");
    }

    // ========== 限流 ==========

    @Test
    void chat_shouldReturn429OnRateLimit() {
        when(rateLimiter.allowRequest(anyString())).thenReturn(false);

        ChatRequest request = new ChatRequest("user1", "借一台 MacBook");
        Result<ChatController.ChatResponse> result = controller.chat(request);

        assertThat(result.getCode()).isEqualTo(429);
        assertThat(result.getMsg()).contains("请求过于频繁");
    }

    // ========== 默认 userId ==========

    @Test
    void chat_shouldUseDefaultUserIdWhenNull() {
        when(rateLimiter.allowRequest("web-user")).thenReturn(true);
        when(agentLoop.process("web-user", "hello"))
                .thenReturn(AgentResult.finalAnswer("你好"));

        ChatRequest request = new ChatRequest(null, "hello");
        Result<ChatController.ChatResponse> result = controller.chat(request);

        assertThat(result.getCode()).isEqualTo(200);
    }
}
