package com.helmsail.lightborrow.mcp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MCP 模块配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "lightborrow.mcp")
public class McpProperties {

    /** 工具扫描的基础包，为空则扫描全部 */
    private String basePackage = "";
}
