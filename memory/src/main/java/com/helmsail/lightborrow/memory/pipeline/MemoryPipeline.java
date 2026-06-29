package com.helmsail.lightborrow.memory.pipeline;

import com.helmsail.lightborrow.memory.model.MemoryContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MemoryPipeline {

    private final List<MemoryStage> stages;
    private final HistoryStage historyStage;

    public MemoryPipeline(List<MemoryStage> stages, HistoryStage historyStage) {
        this.stages = stages;
        this.historyStage = historyStage;
    }

    public MemoryContext load(String userId, String sessionId) {
        MemoryContext ctx = MemoryContext.builder().userId(userId).sessionId(sessionId).build();
        for (MemoryStage stage : stages) {
            stage.load(ctx);
        }
        log.debug("[Memory] 记忆加载完成 userId={}, sessionId={}", userId, sessionId);
        return ctx;
    }

    public void save(MemoryContext ctx) {
        for (int i = stages.size() - 1; i >= 0; i--) {
            stages.get(i).save(ctx);
        }
        log.debug("[Memory] 记忆保存完成 userId={}", ctx.getUserId());
    }

    public void appendHistory(String userId, String sessionId, String messageJson) {
        historyStage.appendMessage(userId, sessionId, messageJson);
    }
}
