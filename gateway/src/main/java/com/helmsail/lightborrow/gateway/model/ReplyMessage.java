package com.helmsail.lightborrow.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回复消息。AgentLoop 处理完成后，构建 ReplyMessage 返回给用户。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyMessage {

    /** 对应 InternalMessage.msgId */
    private String msgId;

    /** 用户 ID */
    private String userId;

    /** 渠道：feishu / dingtalk / wechat */
    private String channel;

    /** 回复内容 */
    private String content;

    /** 消息类型 */
    private ReplyType type;
}
