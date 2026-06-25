package com.helmsail.lightborrow.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@Data
@ConfigurationProperties(prefix = "lightborrow.http")
public class HttpProperties {

    /** 连接超时 */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /** 读取超时 */
    private Duration readTimeout = Duration.ofSeconds(10);
}
