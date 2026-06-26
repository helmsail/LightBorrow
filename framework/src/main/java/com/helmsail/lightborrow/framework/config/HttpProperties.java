package com.helmsail.lightborrow.framework.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * HTTP 客户端配置属性。
 */
@Getter
@Setter
@ToString
@Validated
@ConfigurationProperties(prefix = "lightborrow.http")
public class HttpProperties {

    /** 连接超时 */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /** 读取超时 */
    private Duration readTimeout = Duration.ofSeconds(10);
}
