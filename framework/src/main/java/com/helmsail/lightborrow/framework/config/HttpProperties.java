package com.helmsail.lightborrow.framework.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
@ToString
@Validated
@ConfigurationProperties(prefix = "lightborrow.http")
public class HttpProperties {

    private Duration connectTimeout = Duration.ofSeconds(5);

    private Duration readTimeout = Duration.ofSeconds(10);

    /** 生产环境需显式配置允许的跨域来源 */
    private List<String> allowedOrigins = List.of();
}
