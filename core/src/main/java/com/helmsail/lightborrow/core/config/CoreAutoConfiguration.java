package com.helmsail.lightborrow.core.config;

import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.core.agent.AgentLoop;
import com.helmsail.lightborrow.core.agent.ReActLoop;
import com.helmsail.lightborrow.core.rewrite.CleanRewriteStage;
import com.helmsail.lightborrow.core.rewrite.LlmRewriteStage;
import com.helmsail.lightborrow.core.rewrite.RewritePipeline;
import com.helmsail.lightborrow.core.rewrite.RewriteStage;
import com.helmsail.lightborrow.mcp.registry.ToolRegistry;
import com.helmsail.lightborrow.aiinfra.config.AiAutoConfiguration;
import com.helmsail.lightborrow.mcp.config.McpAutoConfiguration;
import com.helmsail.lightborrow.memory.config.MemoryAutoConfiguration;
import com.helmsail.lightborrow.memory.pipeline.MemoryPipeline;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
@AutoConfigureAfter({MemoryAutoConfiguration.class, AiAutoConfiguration.class, McpAutoConfiguration.class})
@EnableConfigurationProperties(CoreProperties.class)
public class CoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CleanRewriteStage cleanRewriteStage() {
        return new CleanRewriteStage();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ChatModel.class)
    public LlmRewriteStage llmRewriteStage(ChatModel chatModel) {
        return new LlmRewriteStage(chatModel);
    }

    @Bean
    @ConditionalOnMissingBean
    public RewritePipeline rewritePipeline(List<RewriteStage> stages) {
        return new RewritePipeline(stages);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ChatModel.class, ToolRegistry.class})
    public ReActLoop reActLoop(ChatModel chatModel, ToolRegistry toolRegistry,
                               CoreProperties coreProperties) {
        return new ReActLoop(chatModel, toolRegistry, coreProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(MemoryPipeline.class)
    public AgentLoop agentLoop(MemoryPipeline memoryPipeline,
                               RewritePipeline rewritePipeline,
                               ReActLoop reActLoop) {
        return new AgentLoop(memoryPipeline, rewritePipeline, reActLoop);
    }
}
