package com.helmsail.lightborrow.gateway.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmsail.lightborrow.gateway.config.GatewayProperties;
import com.helmsail.lightborrow.gateway.exception.GatewayException;
import com.helmsail.lightborrow.gateway.model.InternalMessage;
import com.helmsail.lightborrow.gateway.model.ReplyMessage;
import com.helmsail.lightborrow.gateway.util.SignUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.helmsail.lightborrow.framework.constant.ErrorCode.GATEWAY_CHANNEL_ERROR;

/**
 * 飞书消息适配器。
 *
 * <p>验签算法：Base64(HmacSHA256(appSecret, timestamp + "\n" + appSecret))。
 * <br>请求头：X-Lark-Request-Timestamp、X-Lark-Request-Nonce、X-Lark-Signature
 */
@Slf4j
public class FeishuAdapter implements ChannelAdapter {

    private static final String CHANNEL = "feishu";

    private static final String HEADER_TIMESTAMP = "x-lark-request-timestamp";
    private static final String HEADER_SIGNATURE = "x-lark-signature";

    private final GatewayProperties gatewayProperties;
    private final ObjectMapper objectMapper;

    public FeishuAdapter(GatewayProperties gatewayProperties, ObjectMapper objectMapper) {
        this.gatewayProperties = gatewayProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public InternalMessage parseRequest(String channel, String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            JsonNode event = root.path("event");
            JsonNode message = event.path("message");

            String msgId = message.path("message_id").asText("");
            String userId = extractUserId(rawBody);
            String chatId = extractChatId(rawBody);
            String content = message.path("content").asText("");

            return InternalMessage.builder()
                    .msgId(msgId)
                    .userId(userId)
                    .chatId(chatId)
                    .channel(CHANNEL)
                    .content(content)
                    .rawData(rawBody)
                    .timestamp(System.currentTimeMillis())
                    .build();
        } catch (JsonProcessingException e) {
            log.error("[Feishu] 消息解析失败", e);
            throw new GatewayException(GATEWAY_CHANNEL_ERROR, e, "飞书消息格式异常");
        }
    }

    @Override
    public String formatReply(ReplyMessage reply) {
        // 实际实现：格式化为飞书消息 JSON
        return reply.getContent();
    }

    @Override
    public void verifyRequest(Map<String, String> headers, String body) throws GatewayException {
        GatewayProperties.ChannelConfig config = gatewayProperties.getChannels().get(CHANNEL);
        if (config == null || config.getAppSecret() == null) {
            log.warn("[Feishu] 未配置 appSecret，跳过验签");
            return;
        }

        String timestamp = headers.get(HEADER_TIMESTAMP);
        String signature = headers.get(HEADER_SIGNATURE);

        if (timestamp == null || signature == null) {
            log.warn("[Feishu] 缺少验签头 x-lark-request-timestamp / x-lark-signature");
            throw new GatewayException(GATEWAY_CHANNEL_ERROR, "飞书请求缺少签名头");
        }

        String expected = SignUtils.hmacSha256(config.getAppSecret(), timestamp);
        if (!expected.equals(signature)) {
            log.warn("[Feishu] 签名验证失败");
            throw new GatewayException(GATEWAY_CHANNEL_ERROR, "飞书签名验证失败");
        }

        log.debug("[Feishu] 验签通过");
    }

    @Override
    public String extractUserId(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            return root.path("event").path("sender").path("sender_id").path("user_id").asText("");
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    @Override
    public String extractChatId(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            return root.path("event").path("message").path("chat_id").asText("");
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
