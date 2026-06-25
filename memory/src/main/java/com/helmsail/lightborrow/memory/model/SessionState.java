package com.helmsail.lightborrow.memory.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 简化版会话状态。存储在 Redis Hash {@code session:{userId}}。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionState {

    private String userId;

    /** LLM 最近一次识别的意图（仅供参考） */
    private String intent;

    /** ACTIVE / COMPLETED */
    private String status;

    /** 会话创建时间戳 */
    private long createdAt;

    /** 最后访问时间戳 */
    private long lastAccessAt;
}
