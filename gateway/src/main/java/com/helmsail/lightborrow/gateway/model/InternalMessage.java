package com.helmsail.lightborrow.gateway.model;

import lombok.*;

/**
 * 统一内部消息模型。Gateway 从 IM 平台收到消息后转换为 InternalMessage。
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalMessage {

    /** 消息 ID（用于幂等） */
    private String msgId;

    /** 用户 ID */
    private String userId;

    /** 群聊/会话 ID（区分单聊与群聊） */
    private String chatId;

    /** 渠道：feishu / dingtalk / wechat */
    private String channel;

    /** 消息文本内容 */
    private String content;

    /** 原始请求体 JSON（用于调试与稽核） */
    private String rawData;

    /** 消息时间戳 */
    private Long timestamp;
}
