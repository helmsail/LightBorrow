package com.helmsail.lightborrow;

import com.helmsail.lightborrow.core.agent.AgentLoop;
import com.helmsail.lightborrow.core.agent.AgentResult;
import com.helmsail.lightborrow.core.agent.AgentResultType;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ChatController controller;

    // ========== 正常消息流程 ==========

    @Test
    void chat_shouldReturnFinalAnswer() {
        when(agentLoop.process(any(), any(), any()))
                .thenReturn(AgentResult.finalAnswer("你已成功借出 MacBook Pro"));

        ChatRequest request = new ChatRequest("user1", null, "借一台 MacBook");
        Result<ChatController.ChatResponse> result = controller.chat(request);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().type()).isEqualTo(AgentResultType.FINAL_ANSWER);
        assertThat(result.getData().content()).isEqualTo("你已成功借出 MacBook Pro");
    }

    @Test
    void chat_shouldReturnQuestion() {
        when(agentLoop.process(any(), any(), any()))
                .thenReturn(AgentResult.question("你想借用什么设备？"));

        ChatRequest request = new ChatRequest("user1", null, "我想借用设备");
        Result<ChatController.ChatResponse> result = controller.chat(request);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().type()).isEqualTo(AgentResultType.QUESTION);
        assertThat(result.getData().content()).isEqualTo("你想借用什么设备？");
    }

    @Test
    void chat_shouldReturnConfirm() {
        when(agentLoop.process(any(), any(), any()))
                .thenReturn(AgentResult.confirm("确认借用 MacBook Pro？"));

        ChatRequest request = new ChatRequest("user1", null, "确认");
        Result<ChatController.ChatResponse> result = controller.chat(request);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().type()).isEqualTo(AgentResultType.CONFIRM);
        assertThat(result.getData().content()).isEqualTo("确认借用 MacBook Pro？");
    }

    @Test
    void chat_shouldReturnError() {
        when(agentLoop.process(any(), any(), any()))
                .thenReturn(AgentResult.error("系统繁忙"));

        ChatRequest request = new ChatRequest("user1", null, "test");
        Result<ChatController.ChatResponse> result = controller.chat(request);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().type()).isEqualTo(AgentResultType.ERROR);
        assertThat(result.getData().content()).isEqualTo("系统繁忙");
    }

    // ========== 默认 userId ==========

    @Test
    void chat_shouldHandleNullUserIdWithUuid() {
        when(agentLoop.process(any(), any(), any()))
                .thenReturn(AgentResult.finalAnswer("你好"));

        ChatRequest request = new ChatRequest(null, null, "hello");
        Result<ChatController.ChatResponse> result = controller.chat(request);

        assertThat(result.getCode()).isEqualTo(200);
    }
}
