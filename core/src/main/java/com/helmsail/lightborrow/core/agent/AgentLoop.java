package com.helmsail.lightborrow.core.agent;

import com.helmsail.lightborrow.aiinfra.model.ChatMessage;
import com.helmsail.lightborrow.core.model.ConversationContext;
import com.helmsail.lightborrow.core.rewrite.RewritePipeline;
import com.helmsail.lightborrow.framework.util.JsonUtil;
import com.helmsail.lightborrow.memory.model.MemoryContext;
import com.helmsail.lightborrow.memory.pipeline.MemoryPipeline;
import com.helmsail.lightborrow.memory.service.LongTermMemoryService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AgentLoop {

    private final MemoryPipeline memoryPipeline;
    private final RewritePipeline rewritePipeline;
    private final ReActLoop reActLoop;
    private final InputGuardFilter inputGuardFilter;
    private final MemoryExtractor memoryExtractor;
    private final LongTermMemoryService longTermMemoryService;

    public AgentLoop(MemoryPipeline memoryPipeline,
                     RewritePipeline rewritePipeline,
                     ReActLoop reActLoop,
                     InputGuardFilter inputGuardFilter,
                     MemoryExtractor memoryExtractor,
                     LongTermMemoryService longTermMemoryService) {
        this.memoryPipeline = memoryPipeline;
        this.rewritePipeline = rewritePipeline;
        this.reActLoop = reActLoop;
        this.inputGuardFilter = inputGuardFilter;
        this.memoryExtractor = memoryExtractor;
        this.longTermMemoryService = longTermMemoryService;
    }

    public AgentResult process(String userId, String sessionId, String content) {
        return process(userId, sessionId, content, null);
    }

    public AgentResult process(String userId, String sessionId, String content,
                               java.util.function.Consumer<String> progressCallback) {
        log.info("[Agent] 开始处理 userId={}, sessionId={}", userId, sessionId);
        long startTime = System.currentTimeMillis();

        // ========== 输入安全检查 ==========
        InputGuardFilter.GuardResult guardResult = inputGuardFilter.check(content);
        if (!guardResult.passed()) {
            log.warn("[Agent] 输入安全检查未通过 userId={}, reason={}", userId, guardResult.rejectReason());
            return AgentResult.error(guardResult.rejectReason());
        }
        String safeContent = guardResult.sanitizedInput();

        ConversationContext ctx = new ConversationContext(userId, safeContent);

        try {
            if (progressCallback != null) progressCallback.accept("正在加载记忆...");
            MemoryContext memoryCtx = memoryPipeline.load(userId, sessionId);
            ctx.setMemoryContext(memoryCtx);

            // ========== 加载长期记忆 ==========
            if (longTermMemoryService != null && memoryExtractor != null) {
                List<String> memories = longTermMemoryService.retrieve(userId, safeContent);
                if (!memories.isEmpty()) {
                    ctx.setLongTermMemories(memories);
                    log.info("[Agent] 加载长期记忆 {} 条 userId={}", memories.size(), userId);
                }
            }

            if (progressCallback != null) progressCallback.accept("正在理解你的问题...");
            rewritePipeline.execute(ctx);

            if (progressCallback != null) progressCallback.accept("正在思考处理方案...");
            reActLoop.execute(ctx);

            if (progressCallback != null) progressCallback.accept("正在保存记录...");
            memoryPipeline.save(memoryCtx);
            saveConversationHistory(ctx, sessionId);

            // Token 用量追踪：记录每次 Agent 处理的总消耗
            long durationMs = System.currentTimeMillis() - startTime;
            int totalMessages = ctx.getMessages().size();
            log.info("[Agent] 处理完成 userId={}, durationMs={}, messages={}",
                    userId, durationMs, totalMessages);

            AgentResult result = buildResult(ctx);
            if (progressCallback != null) {
                String msg = result.getType() == AgentResultType.ERROR
                        ? "处理出错: " + result.getContent()
                        : result.getContent();
                progressCallback.accept(msg);
            }
            return result;

        } catch (Exception e) {
            log.error("[Agent] 处理失败 userId={}", userId, e);
            AgentResult err = AgentResult.error("系统处理失败，请稍后再试。");
            if (progressCallback != null) progressCallback.accept(err.getContent());
            return err;
        }
    }

    private void saveConversationHistory(ConversationContext ctx, String sessionId) {
        try {
            memoryPipeline.appendHistory(ctx.getUserId(), sessionId,
                    JsonUtil.toJson(ChatMessage.user(ctx.getUserInput())));
            if (ctx.getFinalAnswer() != null) {
                memoryPipeline.appendHistory(ctx.getUserId(), sessionId,
                        JsonUtil.toJson(ChatMessage.assistant(ctx.getFinalAnswer())));
            }
        } catch (Exception e) {
            log.warn("[Agent] 历史保存失败 userId={}", ctx.getUserId(), e);
        }
    }

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
