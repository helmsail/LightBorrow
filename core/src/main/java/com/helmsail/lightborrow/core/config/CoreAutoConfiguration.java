package com.helmsail.lightborrow.core.config;

import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.core.agent.AgentLoop;
import com.helmsail.lightborrow.core.agent.InputGuardFilter;
import com.helmsail.lightborrow.core.agent.MemoryExtractor;
import com.helmsail.lightborrow.core.agent.ReActLoop;
import com.helmsail.lightborrow.core.rewrite.CleanRewriteStage;
import com.helmsail.lightborrow.core.rewrite.LlmRewriteStage;
import com.helmsail.lightborrow.core.rewrite.PromptTemplateService;
import com.helmsail.lightborrow.core.rewrite.RewritePipeline;
import com.helmsail.lightborrow.core.rewrite.RewriteStage;
import com.helmsail.lightborrow.core.rewrite.RuleRewriteStage;
import com.helmsail.lightborrow.memory.service.LongTermMemoryService;
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
    public InputGuardFilter inputGuardFilter() {
        return new InputGuardFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ChatModel.class)
    public MemoryExtractor memoryExtractor(ChatModel chatModel) {
        return new MemoryExtractor(chatModel);
    }

    @Bean
    @ConditionalOnMissingBean
    public CleanRewriteStage cleanRewriteStage() {
        return new CleanRewriteStage();
    }

    @Bean
    @ConditionalOnMissingBean
    public RuleRewriteStage ruleRewriteStage() {
        return new RuleRewriteStage();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ChatModel.class)
    public LlmRewriteStage llmRewriteStage(ChatModel chatModel,
                                            PromptTemplateService promptTemplateService) {
        return new LlmRewriteStage(chatModel, promptTemplateService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PromptTemplateService promptTemplateService(ToolRegistry toolRegistry) {
        return new PromptTemplateService(toolRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public RewritePipeline rewritePipeline(List<RewriteStage> stages,
                                            CoreProperties coreProperties) {
        return new RewritePipeline(stages, coreProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ChatModel.class, ToolRegistry.class})
    public ReActLoop reActLoop(ChatModel chatModel, ToolRegistry toolRegistry,
                               CoreProperties coreProperties,
                               PromptTemplateService promptTemplateService) {
        return new ReActLoop(chatModel, toolRegistry, coreProperties, promptTemplateService);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({MemoryPipeline.class, MemoryExtractor.class, LongTermMemoryService.class})
    public AgentLoop agentLoop(MemoryPipeline memoryPipeline,
                               RewritePipeline rewritePipeline,
                               ReActLoop reActLoop,
                               InputGuardFilter inputGuardFilter,
                               MemoryExtractor memoryExtractor,
                               LongTermMemoryService longTermMemoryService) {
        return new AgentLoop(memoryPipeline, rewritePipeline, reActLoop, inputGuardFilter,
                memoryExtractor, longTermMemoryService);
    }
}
