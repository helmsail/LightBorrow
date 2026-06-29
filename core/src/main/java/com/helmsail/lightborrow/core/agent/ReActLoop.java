package com.helmsail.lightborrow.core.agent;

import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.model.ChatMessage;
import com.helmsail.lightborrow.aiinfra.model.ChatRequest;
import com.helmsail.lightborrow.aiinfra.model.ChatResponse;
import com.helmsail.lightborrow.core.config.CoreProperties;
import com.helmsail.lightborrow.core.exception.CoreException;
import com.helmsail.lightborrow.core.model.ConversationContext;
import com.helmsail.lightborrow.core.rewrite.PromptTemplateService;
import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.util.JsonUtil;
import com.helmsail.lightborrow.mcp.registry.ToolDefinition;
import com.helmsail.lightborrow.mcp.registry.ToolRegistry;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * LLM 驱动的思考-行动-反思循环。
 *
 * <p>支持：Plan-and-Execute（先计划后执行）、并行 Tool 调用、工具错误恢复、Self-Reflection。
 * 基于 OpenAI Function Calling 原生 tool_calls + finishReason 判断。
 */
@Slf4j
public class ReActLoop {

    private static final String FINAL_ANSWER_PREFIX = "FINAL_ANSWER:";

    private final ChatModel chatModel;
    private final ToolRegistry toolRegistry;
    private final int maxSteps;
    private final PromptTemplateService promptTemplateService;
    private final ExecutorService executor;

    public ReActLoop(ChatModel chatModel, ToolRegistry toolRegistry,
                     CoreProperties coreProperties,
                     PromptTemplateService promptTemplateService) {
        this.chatModel = chatModel;
        this.toolRegistry = toolRegistry;
        this.maxSteps = coreProperties.getMaxSteps();
        this.promptTemplateService = promptTemplateService;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        log.info("[ReAct] 虚拟线程执行器已关闭");
    }

    public void execute(ConversationContext ctx) {
        String systemPrompt = buildSystemPrompt(ctx);

        // ========== Phase 0: Plan-and-Execute ==========
        String plan = createPlan(systemPrompt, ctx);
        if (plan != null) {
            log.info("[ReAct] 执行计划: {}", plan);
        }

        for (int step = 0; step < maxSteps; step++) {
            log.debug("[ReAct] 第 {} 步，消息数={}", step + 1, ctx.getMessages().size());

            ChatRequest req = ChatRequest.builder()
                    .model(null)
                    .messages(buildMessages(systemPrompt + (plan != null ? "\n\n## 执行计划\n" + plan : ""), ctx))
                    .temperature(null)
                    .maxTokens(null)
                    .stream(false)
                    .tools(buildToolDefinitions())
                    .build();

            ChatResponse response = chatModel.chat(req);
            if (response == null || (response.content() == null && !response.hasToolCalls())) {
                log.warn("[ReAct] LLM 返回空响应");
                break;
            }

            // ========== Case 1: LLM 调用了工具 ==========
            if (response.hasToolCalls()) {
                if (response.content() != null && !response.content().isBlank()) {
                    ctx.addMessage(ChatMessage.assistant(response.content()));
                }

                List<ChatMessage.ToolCall> toolCalls = response.toolCalls();
                ctx.addMessage(ChatMessage.assistantWithToolCalls(toolCalls));

                // 并行执行普���工具，串行处理用户交互工具
                if (executeToolCalls(ctx, toolCalls)) {
                    break; // 用户交互工具触发了等待
                }

                // ========== Self-Reflection ==========
                if (step < maxSteps - 1) {
                    reflect(systemPrompt, ctx);
                }
                continue;
            }

            String content = response.content();

            // ========== Case 2: 兼容旧版 FINAL_ANSWER: 前缀 ==========
            if (content != null && content.startsWith(FINAL_ANSWER_PREFIX)) {
                String finalAnswer = content.substring(FINAL_ANSWER_PREFIX.length()).trim();
                ctx.setFinalAnswer(finalAnswer);
                ctx.addMessage(ChatMessage.assistant(finalAnswer));
                log.info("[ReAct] LLM 返回最终回答 (FINAL_ANSWER 标记)");
                break;
            }

            // ========== Case 3: 根据 finishReason 判断 ==========
            String finishReason = response.finishReason();
            if ("stop".equals(finishReason)) {
                ctx.setFinalAnswer(content);
                ctx.addMessage(ChatMessage.assistant(content));
                log.info("[ReAct] LLM 返回最终回答 (finishReason=stop)");
                break;
            }

            if ("length".equals(finishReason)) {
                log.warn("[ReAct] LLM Token 耗尽，返回截断结果");
                ctx.setFinalAnswer(content);
                ctx.addMessage(ChatMessage.assistant(content));
                break;
            }

            log.debug("[ReAct] LLM 中间思考 (finishReason={})", finishReason);
            ctx.addMessage(ChatMessage.assistant(content));
        }

        if (ctx.getFinalAnswer() == null && !ctx.isAwaitingUser() && !ctx.isAwaitingConfirm()) {
            throw new CoreException(ErrorCode.CORE_REACT_MAX_STEPS, ctx.getUserId());
        }
    }

    /** Phase 0: 制定执行计划。返回计划文本，null 表示不需要计划。 */
    private String createPlan(String systemPrompt, ConversationContext ctx) {
        String planPrompt = promptTemplateService.getRaw("prompts/plan-prompt.md");
        if (planPrompt == null || planPrompt.isBlank()) return null;

        ChatRequest req = ChatRequest.builder()
                .model(null)
                .messages(List.of(
                        ChatMessage.system(systemPrompt + "\n\n" + planPrompt),
                        ChatMessage.user(ctx.getRewrittenInput() != null
                                ? ctx.getRewrittenInput() : ctx.getUserInput())))
                .temperature(0.3)
                .maxTokens(512)
                .stream(false)
                .tools(buildToolDefinitions())
                .build();

        try {
            ChatResponse response = chatModel.chat(req);
            if (response != null && response.content() != null && !response.content().isBlank()) {
                return response.content().trim();
            }
        } catch (Exception e) {
            log.warn("[ReAct] 计划生成失败，直接执行", e);
        }
        return null;
    }

    /** Self-Reflection：执行工具后让 LLM 反思结果是否合理。 */
    private String reflect(String systemPrompt, ConversationContext ctx) {
        String reflectionPrompt = promptTemplateService.getRaw("prompts/reflection-prompt.md");
        if (reflectionPrompt == null || reflectionPrompt.isBlank()) return null;

        List<ChatMessage> reflectionMessages = new ArrayList<>(ctx.getMessages());
        reflectionMessages.add(ChatMessage.user(reflectionPrompt));

        ChatRequest req = ChatRequest.builder()
                .model(null)
                .messages(reflectionMessages)
                .temperature(0.3)
                .maxTokens(512)
                .stream(false)
                .tools(buildToolDefinitions())
                .build();

        try {
            ChatResponse response = chatModel.chat(req);
            if (response != null && response.content() != null && !response.content().isBlank()) {
                String reflection = response.content().trim();
                log.debug("[ReAct] 自我反思: {}", reflection);

                // 如果反思认为需要继续，返回 true；否则返回要继续的内容
                if (reflection.contains("需要") || reflection.contains("不够") || reflection.contains("还需")) {
                    return reflection;
                }
            }
        } catch (Exception e) {
            log.warn("[ReAct] 反思失败", e);
        }
        return null;
    }

    /** 执行工具调用列表。普通工具并行执行，用户交互工具串行。返回 true 表示需要等待用户。 */
    private boolean executeToolCalls(ConversationContext ctx, List<ChatMessage.ToolCall> toolCalls) {
        boolean hasInteraction = false;
        List<ChatMessage.ToolCall> regularTools = new ArrayList<>();

        for (ChatMessage.ToolCall tc : toolCalls) {
            String toolName = tc.function().name();
            Map<String, Object> args = parseArgs(tc.function().arguments());

            if ("ask_user_question".equals(toolName)) {
                String question = (String) args.get("input");
                ctx.setAwaitingUser(true);
                ctx.setPendingQuestion(question);
                ctx.addMessage(ChatMessage.tool(
                        "[系统已向用户提问，等待用户回答]", tc.id(), toolName));
                log.info("[ReAct] LLM 向用户提问: {}", question);
                return true;

            } else if ("ask_user_confirm".equals(toolName)) {
                String summary = (String) args.get("input");
                ctx.setAwaitingConfirm(true);
                ctx.setPendingConfirmSummary(summary);
                ctx.addMessage(ChatMessage.tool(
                        "[系统已请求用户确认，等待用户确认]", tc.id(), toolName));
                log.info("[ReAct] LLM 请求用户确认: {}", summary);
                return true;

            } else {
                regularTools.add(tc);
            }
        }

        if (regularTools.isEmpty()) return false;

        // 并行执行普通工具
        List<CompletableFuture<Void>> futures = regularTools.stream()
                .map(tc -> CompletableFuture.runAsync(() -> {
                    try {
                        String result = toolRegistry.invoke(tc.function().name(), parseArgs(tc.function().arguments()));
                        ctx.addMessage(ChatMessage.tool(result, tc.id(), tc.function().name()));
                    } catch (Exception e) {
                        log.warn("[ReAct] 工具执行失败 name={}", tc.function().name(), e);
                        String errorMsg = "工具执行失败: " + e.getMessage()
                                + "。请考虑使用其他工具或告诉用户错误原因。";
                        ctx.addMessage(ChatMessage.tool(errorMsg, tc.id(), tc.function().name()));
                    }
                }, executor))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return false;
    }

    private String buildSystemPrompt(ConversationContext ctx) {
        return promptTemplateService.render("prompts/system-prompt.md", ctx);
    }

    private List<ChatMessage> buildMessages(String systemPrompt, ConversationContext ctx) {
        List<ChatMessage> messages = new ArrayList<>(4);
        messages.add(ChatMessage.system(systemPrompt));
        messages.add(ChatMessage.user(ctx.getRewrittenInput() != null
                ? ctx.getRewrittenInput() : ctx.getUserInput()));
        messages.addAll(ctx.getMessages());
        return messages;
    }

    private List<Map<String, Object>> buildToolDefinitions() {
        List<ToolDefinition> tools = toolRegistry.getToolDefinitions();
        return tools.stream().map(tool -> {
            Map<String, Object> function = new LinkedHashMap<>(4);
            function.put("name", tool.getName());
            function.put("description", tool.getDescription());
            function.put("parameters", tool.getParameters());

            Map<String, Object> entry = new LinkedHashMap<>(2);
            entry.put("type", "function");
            entry.put("function", function);
            return entry;
        }).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseArgs(String arguments) {
        if (arguments == null || arguments.isBlank()) {
            return new HashMap<>(4);
        }
        return JsonUtil.fromJson(arguments, Map.class);
    }
}
