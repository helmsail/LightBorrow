package com.helmsail.lightborrow.memory.pipeline;

import com.helmsail.lightborrow.memory.model.MemoryContext;

/**
 * Pipeline 模式：load 顺序执行，save 逆序执行。
 */
public interface MemoryStage {

    /** 加载记忆数据到 MemoryContext。 */
    void load(MemoryContext ctx);

    /** 保存记忆数据。默认空实现。 */
    default void save(MemoryContext ctx) {}
}
