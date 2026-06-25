package com.helmsail.lightborrow.memory.pipeline;

import com.helmsail.lightborrow.memory.model.MemoryContext;

/**
 * 记忆阶段接口。每个 MemoryStage 负责从特定数据源加载/保存一部分记忆。
 * <p>
 * Pipeline 模式：load 顺序执行（Session → History → Profile），save 逆序执行。
 * </p>
 */
public interface MemoryStage {

    /** 加载记忆数据到 MemoryContext。 */
    void load(MemoryContext ctx);

    /** 保存记忆数据。默认空实现。 */
    default void save(MemoryContext ctx) {}
}
