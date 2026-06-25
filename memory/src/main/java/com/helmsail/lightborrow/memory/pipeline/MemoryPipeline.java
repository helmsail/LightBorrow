package com.helmsail.lightborrow.memory.pipeline;

import com.helmsail.lightborrow.memory.model.MemoryContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 记忆管道。编排所有 MemoryStage，按序加载、逆序保存。
 */
@Slf4j
@RequiredArgsConstructor
public class MemoryPipeline {

    private final List<MemoryStage> stages;
    private final HistoryStage historyStage;

    /** 按序加载所有阶段的记忆数据。 */
    public MemoryContext load(String userId) {
        MemoryContext ctx = MemoryContext.builder().userId(userId).build();
        for (MemoryStage stage : stages) {
            stage.load(ctx);
        }
        log.debug("[Memory] 记忆加载完成 userId={}", userId);
        return ctx;
    }

    /** 逆序保存所有阶段的记忆数据。 */
    public void save(MemoryContext ctx) {
        for (int i = stages.size() - 1; i >= 0; i--) {
            stages.get(i).save(ctx);
        }
        log.debug("[Memory] 记忆保存完成 userId={}", ctx.getUserId());
    }

    /**
     * 追加单条消息到对话历史。
     *
     * @param userId      用户 ID
     * @param messageJson 序列化为 JSON 的消息
     */
    public void appendHistory(String userId, String messageJson) {
        historyStage.appendMessage(userId, messageJson);
    }
}
