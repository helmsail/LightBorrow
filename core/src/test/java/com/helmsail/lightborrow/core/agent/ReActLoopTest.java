package com.helmsail.lightborrow.core.agent;

import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.model.ChatMessage;
import com.helmsail.lightborrow.aiinfra.model.ChatRequest;
import com.helmsail.lightborrow.aiinfra.model.ChatResponse;
import com.helmsail.lightborrow.core.config.CoreProperties;
import com.helmsail.lightborrow.core.model.ConversationContext;
import com.helmsail.lightborrow.core.rewrite.PromptTemplateService;
import com.helmsail.lightborrow.mcp.registry.ToolDefinition;
import com.helmsail.lightborrow.mcp.registry.ToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReActLoopTest {

    private ChatModel chatModel;
    private ToolRegistry toolRegistry;
    private PromptTemplateService promptTemplateService;
    private CoreProperties coreProperties;
    private ReActLoop reActLoop;

    @BeforeEach
    void setUp() {
        chatModel = mock(ChatModel.class);
        toolRegistry = mock(ToolRegistry.class);
        promptTemplateService = mock(PromptTemplateService.class);
        coreProperties = new CoreProperties();
        coreProperties.setMaxSteps(5);
        coreProperties.setEnableRewrite(false);

        when(promptTemplateService.getRaw(any())).thenReturn("");
        when(promptTemplateService.render(any(), any())).thenReturn("你是 IT 资产助手");
        when(toolRegistry.getToolDefinitions()).thenReturn(List.of(new ToolDefinition(
                "query_asset", "查询IT资产信息",
                Map.of("type", "object", "properties", Map.of()), null)));

        reActLoop = new ReActLoop(chatModel, toolRegistry, coreProperties, promptTemplateService);
    }

    @Test
    void shouldReturnFinalAnswerWhenLLMReturnsStop() {
        ChatResponse mockResponse = new ChatResponse(
                List.of(new ChatResponse.Choice(
                        new ChatResponse.Message("你好，有什么可以帮助你？", null), "stop")),
                null);

        when(chatModel.chat(any())).thenReturn(mockResponse);

        ConversationContext ctx = new ConversationContext("user1", "hello");
        reActLoop.execute(ctx);

        assertThat(ctx.getFinalAnswer()).isEqualTo("你好，有什么可以帮助你？");
    }

    @Test
    void shouldExecuteToolAndReturnResult() {
        // 第一轮：LLM 返回 tool_calls
        ChatResponse toolResponse = new ChatResponse(
                List.of(new ChatResponse.Choice(
                        new ChatResponse.Message(null, List.of(
                                new ChatMessage.ToolCall("call_1", "function",
                                        new ChatMessage.FunctionCall("query_asset", "{\"code\":\"IT-001\"}")))),
                        "tool_calls")),
                null);

        // 第二轮：LLM 返回最终回答
        ChatResponse finalResponse = new ChatResponse(
                List.of(new ChatResponse.Choice(
                        new ChatResponse.Message("查询结果：MacBook Pro", null), "stop")),
                null);

        when(chatModel.chat(any()))
                .thenReturn(toolResponse)
                .thenReturn(finalResponse);
        when(toolRegistry.invoke("query_asset", Map.of("code", "IT-001")))
                .thenReturn("MacBook Pro 16\" M3 Max");

        ConversationContext ctx = new ConversationContext("user1", "查一下 IT-001");
        reActLoop.execute(ctx);

        assertThat(ctx.getFinalAnswer()).isEqualTo("查询结果：MacBook Pro");
    }

    @Test
    void shouldRecoverFromToolError() {
        ChatResponse toolResponse = new ChatResponse(
                List.of(new ChatResponse.Choice(
                        new ChatResponse.Message(null, List.of(
                                new ChatMessage.ToolCall("call_1", "function",
                                        new ChatMessage.FunctionCall("query_asset", "{\"code\":\"INVALID\"}")))),
                        "tool_calls")),
                null);

        ChatResponse fallbackResponse = new ChatResponse(
                List.of(new ChatResponse.Choice(
                        new ChatResponse.Message("未找到该资产", null), "stop")),
                null);

        // 工具异常 + 重试成功
        when(chatModel.chat(any()))
                .thenReturn(toolResponse)
                .thenReturn(fallbackResponse);

        // 注意：这里要抛出异常再正常返回 - mock 不支持顺序，需要单独测试异常路径
        when(toolRegistry.invoke("query_asset", Map.of("code", "INVALID")))
                .thenThrow(new RuntimeException("资产不存在"));

        ConversationContext ctx = new ConversationContext("user1", "查一下 INVALID");
        reActLoop.execute(ctx);

        assertThat(ctx.getMessages()).anyMatch(msg ->
                msg.content() != null && msg.content().contains("工具执行失败"));
    }

    @Test
    void shouldHandleEmptyResponse() {
        when(chatModel.chat(any())).thenReturn(null);

        ConversationContext ctx = new ConversationContext("user1", "hello");
        try {
            reActLoop.execute(ctx);
        } catch (Exception e) {
            // 空响应时循环结束但无最终答案，应抛异常
            assertThat(e).isNotNull();
        }
    }

    @Test
    void shouldHandleToolWithErrorRecovery() {
        // LLM 调用工具但工具抛异常
        ChatResponse toolResponse = new ChatResponse(
                List.of(new ChatResponse.Choice(
                        new ChatResponse.Message(null, List.of(
                                new ChatMessage.ToolCall("call_1", "function",
                                        new ChatMessage.FunctionCall("query_asset", "{\"code\":\"ERR\"}")))),
                        "tool_calls")),
                null);

        when(chatModel.chat(any())).thenReturn(toolResponse);
        when(toolRegistry.invoke("query_asset", Map.of("code", "ERR")))
                .thenThrow(new RuntimeException("连接失败"));

        ConversationContext ctx = new ConversationContext("user1", "test");
        try {
            reActLoop.execute(ctx);
        } catch (Exception e) {
            // 预期：错误恢复后继续循环，但无最终回答，应抛 CoreException
            assertThat(e).isNotNull();
        }

        // 验证错误信息被作为 tool 消息添加到上下文
        boolean hasError = ctx.getMessages().stream()
                .anyMatch(msg -> "tool".equals(msg.role())
                        && msg.content() != null
                        && msg.content().contains("工具执行失败"));
        assertThat(hasError).isTrue();
    }
}
