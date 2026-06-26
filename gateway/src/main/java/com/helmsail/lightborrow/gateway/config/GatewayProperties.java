package com.helmsail.lightborrow.gateway.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Gateway 模块配置。
 *
 * <p>配置示例：
 * <pre>{@code
 * lightborrow:
 *   gateway:
 *     rate-limit-window-seconds: 60
 *     rate-limit-max-requests: 20
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
}
