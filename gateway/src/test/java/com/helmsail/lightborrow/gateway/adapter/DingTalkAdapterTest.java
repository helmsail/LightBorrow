package com.helmsail.lightborrow.gateway.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmsail.lightborrow.gateway.config.GatewayProperties;
import com.helmsail.lightborrow.gateway.exception.GatewayException;
import com.helmsail.lightborrow.gateway.model.InternalMessage;
import com.helmsail.lightborrow.gateway.util.SignUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static com.helmsail.lightborrow.framework.constant.ErrorCode.GATEWAY_CHANNEL_ERROR;

class DingTalkAdapterTest {

    private static final String CHANNEL = "dingtalk";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private GatewayProperties gatewayProperties;
    private DingTalkAdapter adapter;

    @BeforeEach
    void setUp() {
        gatewayProperties = mock(GatewayProperties.class);
        adapter = new DingTalkAdapter(gatewayProperties, objectMapper);
    }

    @Test
    void shouldReturnDingTalkChannel() {
        assertThat(adapter.getChannel()).isEqualTo(CHANNEL);
    }

    @Test
    void shouldParseDingTalkRequest() {
        String body = """
                {"header":{"msgId":"msg_001"},"text":{"content":"hello"},"conversationId":"chat_001","senderStaffId":"user_001"}""";

        InternalMessage msg = adapter.parseRequest(CHANNEL, body);

        assertThat(msg.getMsgId()).isEqualTo("msg_001");
        assertThat(msg.getUserId()).isEqualTo("user_001");
        assertThat(msg.getChatId()).isEqualTo("chat_001");
        assertThat(msg.getChannel()).isEqualTo(CHANNEL);
        assertThat(msg.getContent()).isEqualTo("hello");
    }

    @Test
    void shouldThrowOnMalformedJson() {
        assertThatThrownBy(() -> adapter.parseRequest(CHANNEL, "{invalid}"))
                .isInstanceOf(GatewayException.class)
                .satisfies(e -> assertThat(((GatewayException) e).getCode()).isEqualTo(GATEWAY_CHANNEL_ERROR.getCode()));
    }

    @Test
    void formatReplyShouldReturnContent() {
        var reply = com.helmsail.lightborrow.gateway.model.ReplyMessage.builder()
                .content("回复内容")
                .build();
        assertThat(adapter.formatReply(reply)).isEqualTo("回复内容");
    }

    @Test
    void verifyRequestShouldPassForValidSignature() {
        GatewayProperties.ChannelConfig config = new GatewayProperties.ChannelConfig();
        config.setAppSecret("test_secret");
        when(gatewayProperties.getChannels()).thenReturn(Map.of(CHANNEL, config));

        String timestamp = "1719360000000";
        String expectedSign = SignUtils.hmacSha256("test_secret", timestamp);
        Map<String, String> headers = Map.of(
                "timestamp", timestamp,
                "sign", expectedSign);

        adapter.verifyRequest(headers, "body");
        // Should not throw
    }

    @Test
    void verifyRequestShouldSkipWhenNoConfig() {
        when(gatewayProperties.getChannels()).thenReturn(Map.of());

        adapter.verifyRequest(Map.of(), "body");
        // Should not throw
    }

    @Test
    void verifyRequestShouldThrowOnMissingHeaders() {
        GatewayProperties.ChannelConfig config = new GatewayProperties.ChannelConfig();
        config.setAppSecret("secret");
        when(gatewayProperties.getChannels()).thenReturn(Map.of(CHANNEL, config));

        assertThatThrownBy(() -> adapter.verifyRequest(Map.of(), "body"))
                .isInstanceOf(GatewayException.class)
                .satisfies(e -> assertThat(((GatewayException) e).getCode()).isEqualTo(GATEWAY_CHANNEL_ERROR.getCode()));
    }

    @Test
    void verifyRequestShouldThrowOnInvalidSignature() {
        GatewayProperties.ChannelConfig config = new GatewayProperties.ChannelConfig();
        config.setAppSecret("secret");
        when(gatewayProperties.getChannels()).thenReturn(Map.of(CHANNEL, config));

        Map<String, String> headers = Map.of(
                "timestamp", "1719360000000",
                "sign", "invalid_signature");

        assertThatThrownBy(() -> adapter.verifyRequest(headers, "body"))
                .isInstanceOf(GatewayException.class)
                .satisfies(e -> assertThat(((GatewayException) e).getCode()).isEqualTo(GATEWAY_CHANNEL_ERROR.getCode()));
    }

    @Test
    void extractUserIdShouldReturnSenderStaffId() {
        String body = "{\"senderStaffId\":\"user_001\"}";
        assertThat(adapter.extractUserId(body)).isEqualTo("user_001");
    }

    @Test
    void extractUserIdShouldReturnSenderIdAsFallback() {
        String body = "{\"senderId\":\"user_002\"}";
        assertThat(adapter.extractUserId(body)).isEqualTo("user_002");
    }

    @Test
    void extractUserIdShouldReturnEmptyForInvalidJson() {
        assertThat(adapter.extractUserId("{invalid}")).isEmpty();
    }

    @Test
    void extractChatIdShouldReturnConversationId() {
        String body = "{\"conversationId\":\"chat_001\"}";
        assertThat(adapter.extractChatId(body)).isEqualTo("chat_001");
    }

    @Test
    void extractChatIdShouldReturnEmptyForInvalidJson() {
        assertThat(adapter.extractChatId("{invalid}")).isEmpty();
    }
}
