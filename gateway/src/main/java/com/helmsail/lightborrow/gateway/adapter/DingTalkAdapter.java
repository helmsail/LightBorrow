package com.helmsail.lightborrow.gateway.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmsail.lightborrow.gateway.config.GatewayProperties;
import com.helmsail.lightborrow.gateway.exception.GatewayException;
import com.helmsail.lightborrow.gateway.model.InternalMessage;
import com.helmsail.lightborrow.gateway.model.ReplyMessage;
import com.helmsail.lightborrow.gateway.util.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.helmsail.lightborrow.framework.constant.ErrorCode.GATEWAY_CHANNEL_ERROR;

/**
 * 钉钉消息适配器。
 *
 * <p>验签算法：Base64(HmacSHA256(appSecret, timestamp + "\n" + appSecret))。
 * <br>请求头：timestamp（毫秒级）、sign
 */
@Slf4j
@Component
public class DingTalkAdapter implements ChannelAdapter {

    private static final String CHANNEL = "dingtalk";

    private final GatewayProperties gatewayProperties;
    private final ObjectMapper objectMapper;

    public DingTalkAdapter(GatewayProperties gatewayProperties, ObjectMapper objectMapper) {
        this.gatewayProperties = gatewayProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public InternalMessage parseRequest(String channel, String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String msgId = root.path("header").path("msgId").asText("");
            String userId = extractUserId(rawBody);
            String chatId = extractChatId(rawBody);
            String content = root.path("text").path("content").asText("");

            // 兼容不同消息格式：可能是 text / markdown / actionCard 等
            if (content.isEmpty()) {
                content = root.path("content").path("text").asText("");
            }

            return InternalMessage.builder()
                    .msgId(msgId)
                    .userId(userId)
                    .chatId(chatId)
                    .channel(CHANNEL)
                    .content(content)
                    .rawData(rawBody)
                    .timestamp(System.currentTimeMillis())
                    .build();
        } catch (Exception e) {
            log.error("[DingTalk] 消息解析失败", e);
            throw new GatewayException(GATEWAY_CHANNEL_ERROR, e, "钉钉消息格式异常");
        }
    }

    @Override
    public String formatReply(ReplyMessage reply) {
        // 实际实现：格式化为钉钉消息 JSON
        return reply.getContent();
    }

    @Override
    public void verifyRequest(Map<String, String> headers, String body) throws GatewayException {
        GatewayProperties.ChannelConfig config = gatewayProperties.getChannels().get(CHANNEL);
        if (config == null || config.getAppSecret() == null) {
            log.warn("[DingTalk] 未配置 appSecret，跳过验签");
            return;
        }

        String timestamp = headers.get("timestamp");
        String sign = headers.get("sign");

        if (timestamp == null || sign == null) {
            log.warn("[DingTalk] 缺少验签头 timestamp / sign");
            throw new GatewayException(GATEWAY_CHANNEL_ERROR, "钉钉请求缺少签名头");
        }

        String expected = SignUtils.hmacSha256(config.getAppSecret(), timestamp);
        if (!expected.equals(sign)) {
            log.warn("[DingTalk] 签名验证失败");
            throw new GatewayException(GATEWAY_CHANNEL_ERROR, "钉钉签名验证失败");
        }

        log.debug("[DingTalk] 验签通过");
    }

    @Override
    public String extractUserId(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            // 企业内机器人：senderStaffId；群机器人：senderId
            String userId = root.path("senderStaffId").asText("");
            if (userId.isEmpty()) {
                userId = root.path("senderId").asText("");
            }
            return userId;
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String extractChatId(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            return root.path("conversationId").asText("");
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
