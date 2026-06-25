package com.helmsail.lightborrow.mcp.config;

import com.helmsail.lightborrow.mcp.registry.ToolRegistry;
import com.helmsail.lightborrow.mcp.service.AssetService;
import com.helmsail.lightborrow.mcp.tools.AskUserConfirmTool;
import com.helmsail.lightborrow.mcp.tools.AskUserQuestionTool;
import com.helmsail.lightborrow.mcp.tools.AssetTool;
import com.helmsail.lightborrow.mcp.tools.RagSearchTool;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * MCP 模块自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(McpProperties.class)
public class McpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ToolRegistry toolRegistry(ApplicationContext applicationContext) {
        return new ToolRegistry(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public AskUserConfirmTool askUserConfirmTool() {
        return new AskUserConfirmTool();
    }

    @Bean
    @ConditionalOnMissingBean
    public AskUserQuestionTool askUserQuestionTool() {
        return new AskUserQuestionTool();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass({DataSource.class, JdbcTemplate.class})
    public AssetService assetService(JdbcTemplate jdbcTemplate) {
        return new AssetService(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(AssetService.class)
    public AssetTool assetTool(AssetService assetService) {
        return new AssetTool(assetService);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "com.helmsail.lightborrow.rag.pipeline.online.RagOnlinePipeline")
    public RagSearchTool ragSearchTool(
            com.helmsail.lightborrow.rag.pipeline.online.RagOnlinePipeline ragOnlinePipeline) {
        return new RagSearchTool(ragOnlinePipeline);
    }
}
