package com.helmsail.lightborrow.gateway.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Gateway 模块配置。
 *
 * <p>配置示例：
 * <pre>{@code
 * lightborrow:
 *   gateway:
 *     rate-limit-window-seconds: 60
 *     rate-limit-max-requests: 20
 *     channels:
 *       feishu:
 *         app-secret: xxx
 *         verify-token: xxx
 *       dingtalk:
 *         app-secret: xxx
 * }</pre>
 */
@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "lightborrow.gateway")
public class GatewayProperties {

    /** 限流窗口（秒） */
    private int rateLimitWindowSeconds = 60;

    /** 窗口内最大请求数 */
    private int rateLimitMaxRequests = 20;

    /** 各 IM 渠道凭证配置，key 为渠道名（feishu / dingtalk / wechat） */
    private Map<String, ChannelConfig> channels;

    @Getter
    @Setter
    @ToString
    public static class ChannelConfig {

        /** 应用密钥（飞书、钉钉必填） */
        private String appSecret;

        /** 飞书 VerifyToken（事件订阅校验） */
        private String verifyToken;
    }
}
