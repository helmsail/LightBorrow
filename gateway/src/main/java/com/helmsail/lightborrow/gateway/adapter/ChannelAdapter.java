package com.helmsail.lightborrow.gateway.adapter;

import com.helmsail.lightborrow.gateway.exception.GatewayException;
import com.helmsail.lightborrow.gateway.model.InternalMessage;
import com.helmsail.lightborrow.gateway.model.ReplyMessage;

import java.util.Map;

/**
 * IM 平台适配器接口。每个 IM 平台实现一个 Adapter，负责消息解析、回复格式化和请求验签。
 */
public interface ChannelAdapter {

    /**
     * 解析 IM 平台的 HTTP Webhook 请求体为统一 InternalMessage。
     *
     * @param channel   渠道名（feishu / dingtalk / wechat）
     * @param rawBody   IM 平台原始请求体
     * @return 统一内部消息
     */
    InternalMessage parseRequest(String channel, String rawBody);

    /**
     * 将回复格式化为 IM 平台要求的格式。
     *
     * @param reply 统一回复消息
     * @return 格式化后的回复内容
     */
    String formatReply(ReplyMessage reply);

    /**
     * 验证 Webhook 请求签名，确保请求来源合法。
     *
     * @param headers 请求头
     * @param body    原始请求体
     * @throws GatewayException 验签失败时抛出
     */
    void verifyRequest(Map<String, String> headers, String body) throws GatewayException;

    /**
     * 从原始请求 JSON 中提取用户 ID。
     *
     * @param rawBody 原始请求体
     * @return 用户 ID
     */
    String extractUserId(String rawBody);

    /**
     * 从原始请求 JSON 中提取群聊/会话 ID。
     *
     * @param rawBody 原始请求体
     * @return 会话 ID
     */
    String extractChatId(String rawBody);

    /**
     * 获取该 Adapter 支持的渠道名。
     *
     * @return 渠道名，如 feishu / dingtalk / wechat
     */
    String getChannel();
}
