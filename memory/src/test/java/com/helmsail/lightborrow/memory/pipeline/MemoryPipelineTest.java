package com.helmsail.lightborrow.memory.pipeline;

import com.helmsail.lightborrow.memory.model.MemoryContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MemoryPipelineTest {

    @Test
    void loadShouldExecuteAllStagesInOrder() {
        MemoryStage stage1 = mock(MemoryStage.class);
        MemoryStage stage2 = mock(MemoryStage.class);
        MemoryStage stage3 = mock(MemoryStage.class);
        HistoryStage historyStage = mock(HistoryStage.class);

        MemoryPipeline pipeline = new MemoryPipeline(List.of(stage1, stage2, stage3), historyStage);

        MemoryContext ctx = pipeline.load("user123", null);

        assertThat(ctx.getUserId()).isEqualTo("user123");
        verify(stage1).load(any(MemoryContext.class));
        verify(stage2).load(any(MemoryContext.class));
        verify(stage3).load(any(MemoryContext.class));
    }

    @Test
    void saveShouldExecuteAllStagesInReverseOrder() {
        MemoryStage stage1 = mock(MemoryStage.class);
        MemoryStage stage2 = mock(MemoryStage.class);
        MemoryStage stage3 = mock(MemoryStage.class);
        HistoryStage historyStage = mock(HistoryStage.class);

        MemoryPipeline pipeline = new MemoryPipeline(List.of(stage1, stage2, stage3), historyStage);

        MemoryContext ctx = MemoryContext.builder().userId("user123").build();
        pipeline.save(ctx);

        InOrder inOrder = inOrder(stage1, stage2, stage3);
        inOrder.verify(stage3).save(any(MemoryContext.class));
        inOrder.verify(stage2).save(any(MemoryContext.class));
        inOrder.verify(stage1).save(any(MemoryContext.class));
    }

    @Test
    void loadShouldHandleEmptyStageList() {
        HistoryStage historyStage = mock(HistoryStage.class);
        MemoryPipeline pipeline = new MemoryPipeline(List.of(), historyStage);

        MemoryContext ctx = pipeline.load("user123", null);

        assertThat(ctx).isNotNull();
        assertThat(ctx.getUserId()).isEqualTo("user123");
    }

    @Test
    void saveShouldHandleEmptyStageList() {
        HistoryStage historyStage = mock(HistoryStage.class);
        MemoryPipeline pipeline = new MemoryPipeline(List.of(), historyStage);

        MemoryContext ctx = MemoryContext.builder().userId("user123").build();
        pipeline.save(ctx);
    }

    @Test
    void appendHistoryShouldDelegateToHistoryStage() {
        HistoryStage historyStage = mock(HistoryStage.class);
        MemoryPipeline pipeline = new MemoryPipeline(List.of(), historyStage);

        pipeline.appendHistory("user123", null, "{\"msg\":\"hello\"}");

        verify(historyStage).appendMessage("user123", null, "{\"msg\":\"hello\"}");
    }
}
