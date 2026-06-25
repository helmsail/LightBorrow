package com.helmsail.lightborrow.gateway.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Gateway 模块自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayAutoConfiguration {
}
