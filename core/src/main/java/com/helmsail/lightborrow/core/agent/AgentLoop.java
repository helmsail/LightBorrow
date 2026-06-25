package com.helmsail.lightborrow.core.agent;

import com.helmsail.lightborrow.aiinfra.model.ChatMessage;
import com.helmsail.lightborrow.core.model.ConversationContext;
import com.helmsail.lightborrow.core.rewrite.RewritePipeline;
import com.helmsail.lightborrow.framework.util.JsonUtil;
import com.helmsail.lightborrow.memory.model.MemoryContext;
import com.helmsail.lightborrow.memory.pipeline.MemoryPipeline;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentLoop {

    private final MemoryPipeline memoryPipeline;
    private final RewritePipeline rewritePipeline;
    private final ReActLoop reActLoop;

    public AgentLoop(MemoryPipeline memoryPipeline,
                     RewritePipeline rewritePipeline,
                     ReActLoop reActLoop) {
        this.memoryPipeline = memoryPipeline;
        this.rewritePipeline = rewritePipeline;
        this.reActLoop = reActLoop;
    }

    /**
     * 处理用户消息。
     *
     * @param userId  用户 ID
     * @param content 用户消息内容
     * @return 处理结果
     */
    public AgentResult process(String userId, String content) {
        log.info("[Agent] 开始处理 userId={}, content={}", userId, content);
        ConversationContext ctx = new ConversationContext(userId, content);

        try {
            // Step 1: 加载记忆
            MemoryContext memoryCtx = memoryPipeline.load(userId);
            ctx.setMemoryContext(memoryCtx);

            // Step 2: 输入重写
            rewritePipeline.execute(ctx);

            // Step 3: ReAct 循环
            reActLoop.execute(ctx);

            // Step 4: 保存记忆
            memoryPipeline.save(memoryCtx);

            // 保存对话历史
            saveConversationHistory(ctx);

            // Step 5: 构建结果
            return buildResult(ctx);

        } catch (Exception e) {
            log.error("[Agent] 处理失败 userId={}", userId, e);
            return AgentResult.error("系统处理失败，请稍后再试。");
        }
    }

    /** 保存对话历史。 */
    private void saveConversationHistory(ConversationContext ctx) {
        try {
            // 保存用户消息
            memoryPipeline.appendHistory(ctx.getUserId(),
                    JsonUtil.toJson(ChatMessage.user(ctx.getUserInput())));

            // 保存助手消息
            if (ctx.getFinalAnswer() != null) {
                memoryPipeline.appendHistory(ctx.getUserId(),
                        JsonUtil.toJson(ChatMessage.assistant(ctx.getFinalAnswer())));
            }
        } catch (Exception e) {
            log.warn("[Agent] 历史保存失败 userId={}", ctx.getUserId(), e);
        }
    }

    /** 构建 Agent 处理结果。 */
    private AgentResult buildResult(ConversationContext ctx) {
        if (ctx.isAwaitingUser()) {
            return AgentResult.question(ctx.getPendingQuestion());
        }
        if (ctx.isAwaitingConfirm()) {
            return AgentResult.confirm(ctx.getPendingConfirmSummary());
        }
        return AgentResult.finalAnswer(ctx.getFinalAnswer());
    }
}
