package com.helmsail.lightborrow.mcp.config;

import com.helmsail.lightborrow.mcp.registry.ToolRegistry;
import com.helmsail.lightborrow.mcp.mapper.AssetMapper;
import com.helmsail.lightborrow.mcp.mapper.BorrowMapper;
import com.helmsail.lightborrow.mcp.mapper.TransferMapper;
import com.helmsail.lightborrow.mcp.service.AssetService;
import com.helmsail.lightborrow.mcp.tools.AskUserConfirmTool;
import com.helmsail.lightborrow.mcp.tools.AskUserQuestionTool;
import com.helmsail.lightborrow.mcp.tools.AssetTool;
import com.helmsail.lightborrow.mcp.tools.RagSearchTool;
import com.helmsail.lightborrow.rag.config.RagAutoConfiguration;
import com.helmsail.lightborrow.rag.pipeline.online.RagOnlinePipeline;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureAfter(RagAutoConfiguration.class)
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
    public AssetService assetService(AssetMapper assetMapper, BorrowMapper borrowMapper,
                                      TransferMapper transferMapper) {
        return new AssetService(assetMapper, borrowMapper, transferMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(AssetService.class)
    public AssetTool assetTool(AssetService assetService) {
        return new AssetTool(assetService);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RagOnlinePipeline.class)
    public RagSearchTool ragSearchTool(RagOnlinePipeline ragOnlinePipeline) {
        return new RagSearchTool(ragOnlinePipeline);
    }
}
