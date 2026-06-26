package com.helmsail.lightborrow.core.agent;

import com.helmsail.lightborrow.core.config.CoreProperties;
import com.helmsail.lightborrow.core.model.ConversationContext;
import com.helmsail.lightborrow.mcp.registry.ToolDefinition;
import com.helmsail.lightborrow.mcp.registry.ToolRegistry;
import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.model.ChatMessage;
import com.helmsail.lightborrow.aiinfra.model.ChatRequest;
import com.helmsail.lightborrow.aiinfra.model.ChatResponse;
import com.helmsail.lightborrow.framework.constant.ErrorCode;
import com.helmsail.lightborrow.framework.util.JsonUtil;
import com.helmsail.lightborrow.core.exception.CoreException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ReAct 循环。LLM 驱动的思考-行动循环，支持工具调用、反问、确认。
 *
 * <p>核心流程：
 * <ol>
 *   <li>构建 SystemPrompt（含工具描述） + 历史消息 + 用户输入</li>
 *   <li>调用 ChatModel.chat()</li>
 *   <li>LLM 返回 tool_calls → 调用工具 → 结果回填 → 继续循环</li>
 *   <li>LLM 返回 FINAL_ANSWER: → 结束循环</li>
 *   <li>LLM 返回 ask_user_question 调用 → 标记等待用户</li>
 * <li>LLM 返回 ask_user_confirm 调用 → 标记等待确认</li>
 * </ol>
 */
@Slf4j
public class ReActLoop {

    private static final int DEFAULT_MAX_STEPS = 15;
    private static final String FINAL_ANSWER_PREFIX = "FINAL_ANSWER:";

    private final ChatModel chatModel;
    private final ToolRegistry toolRegistry;
    private final int maxSteps;

    public ReActLoop(ChatModel chatModel, ToolRegistry toolRegistry,
                     CoreProperties coreProperties) {
        this.chatModel = chatModel;
        this.toolRegistry = toolRegistry;
        this.maxSteps = coreProperties != null ? coreProperties.getMaxSteps() : DEFAULT_MAX_STEPS;
    }

    /**
     * 执行 ReAct 循环。
     *
     * @param ctx 对话上下文
     */
    public void execute(ConversationContext ctx) {
        String systemPrompt = buildSystemPrompt(ctx);

        for (int step = 0; step < maxSteps; step++) {
            log.debug("[ReAct] 第 {} 步，消息数={}", step + 1, ctx.getMessages().size());

            ChatRequest req = ChatRequest.builder()
                    .model(null) // 使用默认模型
                    .messages(buildMessages(systemPrompt, ctx))
                    .temperature(null)
                    .maxTokens(null)
                    .stream(false)
                    .tools(buildToolDefinitions())
                    .build();

            ChatResponse response = chatModel.chat(req);
            if (response == null || response.content() == null) {
                log.warn("[ReAct] LLM 返回空响应");
                break;
            }

            String content = response.content();

            // Case 1: LLM 返回了 tool_calls
            List<ChatMessage.ToolCall> toolCalls = response.toolCalls();
            if (toolCalls != null && !toolCalls.isEmpty()) {
                // 添加 assistant 消息（含 tool_calls）
                ctx.addMessage(ChatMessage.assistantWithToolCalls(toolCalls));

                for (ChatMessage.ToolCall tc : toolCalls) {
                    String toolName = tc.function().name();
                    Map<String, Object> args = parseArgs(tc.function().arguments());

                    log.info("[ReAct] 调用工具: {} args={}", toolName, args);

                    // 检查是否是系统工具
                    if ("ask_user_question".equals(toolName)) {
                        String question = (String) args.get("input");
                        ctx.setAwaitingUser(true);
                        ctx.setPendingQuestion(question);
                        ctx.addMessage(ChatMessage.tool(
                                "[系统已向用户提问，等待用户回答]",
                                tc.id(), toolName));
                        log.info("[ReAct] LLM 向用户提问: {}", question);
                        break; // 结束循环，等待用户回答

                    } else if ("ask_user_confirm".equals(toolName)) {
                        String summary = (String) args.get("input");
                        ctx.setAwaitingConfirm(true);
                        ctx.setPendingConfirmSummary(summary);
                        ctx.addMessage(ChatMessage.tool(
                                "[系统已请求用户确认，等待用户确认]",
                                tc.id(), toolName));
                        log.info("[ReAct] LLM 请求用户确认: {}", summary);
                        break; // 结束循环，等待用户确认
                    }

                    // 普通工具调用
                    String result = toolRegistry.invoke(toolName, args);
                    ctx.addMessage(ChatMessage.tool(result, tc.id(), toolName));
                }

                if (ctx.isAwaitingUser() || ctx.isAwaitingConfirm()) {
                    break;
                }
                continue; // 继续下一轮，把工具结果给 LLM
            }

            // Case 2: LLM 返回了最终回答
            if (content.startsWith(FINAL_ANSWER_PREFIX)) {
                String finalAnswer = content.substring(FINAL_ANSWER_PREFIX.length()).trim();
                ctx.setFinalAnswer(finalAnswer);
                ctx.addMessage(ChatMessage.assistant(finalAnswer));
                log.info("[ReAct] LLM 返回最终回答");
                break;
            }

            // Case 3: 普通思考/回答
            ctx.addMessage(ChatMessage.assistant(content));
            ctx.setFinalAnswer(content);
            break; // 默认结束
        }

        if (ctx.getFinalAnswer() == null && !ctx.isAwaitingUser() && !ctx.isAwaitingConfirm()) {
            throw new CoreException(ErrorCode.CORE_REACT_MAX_STEPS,
                    ctx.getUserId());
        }
    }

    /** 构建 System Prompt。 */
    private String buildSystemPrompt(ConversationContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是 IT 资产助手「小灯」，帮助员工处理 IT 资产的查询、借用、转借。\n\n");

        sb.append("## 可用工具\n");
        List<ToolDefinition> tools = toolRegistry.getToolDefinitions();
        for (ToolDefinition tool : tools) {
            sb.append("- ").append(tool.getName())
                    .append(": ").append(tool.getDescription()).append("\n");
        }

        sb.append("\n## 行为规则\n");
        sb.append("1. 【反问】如果用户需求信息不全，调用 ask_user_question 向用户提问\n");
        sb.append("2. 【一次一问】一次只问 1-2 个问题，不要一次问完\n");
        sb.append("3. 【确认】借用/转借/取消前，调用 ask_user_confirm 让用户确认\n");
        sb.append("4. 【知识】政策/指南类问题，调用 query_knowledge 检索知识库\n");
        sb.append("5. 【结束】当你给出了最终回答，在前面加上 FINAL_ANSWER:\n");

        // 用户信息
        sb.append("\n## 用户信息\n");
        if (ctx.getMemoryContext() != null) {
            sb.append("- 用户ID: ").append(ctx.getUserId()).append("\n");
            if (ctx.getMemoryContext().getProfileSummary() != null) {
                sb.append("- ").append(ctx.getMemoryContext().getProfileSummary()).append("\n");
            }
        }

        // 对话历史摘要
        if (ctx.getMemoryContext() != null
                && ctx.getMemoryContext().getHistoryMessages() != null
                && !ctx.getMemoryContext().getHistoryMessages().isEmpty()) {
            sb.append("\n## 对话历史（最近 ")
                    .append(ctx.getMemoryContext().getHistoryMessages().size())
                    .append(" 条）\n");
            for (String msg : ctx.getMemoryContext().getHistoryMessages()) {
                sb.append(msg).append("\n");
            }
        }

        return sb.toString();
    }

    /** 构建消息列表。 */
    private List<ChatMessage> buildMessages(String systemPrompt, ConversationContext ctx) {
        List<ChatMessage> messages = new ArrayList<>(4);
        messages.add(ChatMessage.system(systemPrompt));
        messages.add(ChatMessage.user(ctx.getRewrittenInput() != null
                ? ctx.getRewrittenInput() : ctx.getUserInput()));
        messages.addAll(ctx.getMessages());
        return messages;
    }

    /** 构建工具定义列表（OpenAI Function Calling 格式）。 */
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

    /** 解析工具参数 JSON。 */
    @SuppressWarnings("unchecked") // JsonUtil.fromJson 返回 Map，泛型由调用上下文保证
    private Map<String, Object> parseArgs(String arguments) {
        if (arguments == null || arguments.isBlank()) {
            return new HashMap<>(4);
        }
        return JsonUtil.fromJson(arguments, Map.class);
    }
}
