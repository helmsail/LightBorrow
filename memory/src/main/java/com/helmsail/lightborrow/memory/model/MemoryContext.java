package com.helmsail.lightborrow.memory.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 记忆上下文。由 MemoryPipeline 产出，聚合会话状态、对话历史、用户画像。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryContext {

    /** 会话 ID */
    private String userId;

    /** 会话状态 */
    private SessionState sessionState;

    /** 历史消息列表（最近 20 条） */
    private List<String> historyMessages;

    /** 用户画像摘要 */
    private String profileSummary;

    /** 是否为新会话 */
    private boolean newSession;
}
