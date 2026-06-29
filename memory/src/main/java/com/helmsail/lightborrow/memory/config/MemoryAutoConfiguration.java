package com.helmsail.lightborrow.memory.config;

import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.embedding.EmbeddingModel;
import com.helmsail.lightborrow.memory.mapper.BehaviorMapper;
import com.helmsail.lightborrow.memory.mapper.UserMemoryMapper;
import com.helmsail.lightborrow.memory.pipeline.HistoryStage;
import com.helmsail.lightborrow.memory.pipeline.MemoryPipeline;
import com.helmsail.lightborrow.memory.pipeline.MemoryStage;
import com.helmsail.lightborrow.memory.pipeline.ProfileStage;
import com.helmsail.lightborrow.memory.pipeline.SessionStage;
import com.helmsail.lightborrow.memory.pipeline.SummaryStage;
import com.helmsail.lightborrow.memory.service.FeedbackService;
import com.helmsail.lightborrow.memory.service.LongTermMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * Memory 模块自动配置。
 *
 * <p>当 Redis 可用时，使用 Redis 背书的 SessionStage 和 HistoryStage；
 * 当 Redis 不可用时（如 dev-nodocker 模式），Stage 内部自动降级为内存存储。
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(MemoryProperties.class)
public class MemoryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SessionStage sessionStage(ObjectProvider<StringRedisTemplate> templateProvider) {
        StringRedisTemplate template = templateProvider.getIfAvailable();
        if (template != null) {
            log.info("[Memory] Redis 可用，创建 Redis 版 SessionStage");
        } else {
            log.warn("[Memory] Redis 不可用，SessionStage 降级为内存模式（重启后数据丢失）");
        }
        return new SessionStage(template);
    }

    @Bean
    @ConditionalOnMissingBean
    public HistoryStage historyStage(ObjectProvider<StringRedisTemplate> templateProvider,
                                      MemoryProperties properties) {
        StringRedisTemplate template = templateProvider.getIfAvailable();
        if (template != null) {
            log.info("[Memory] Redis 可用，创建 Redis 版 HistoryStage, maxHistory={}",
                    properties.getMaxHistory());
        } else {
            log.warn("[Memory] Redis 不可用，HistoryStage 降级为内存模式（重启后数据丢失）");
        }
        return new HistoryStage(template, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProfileStage profileStage(BehaviorMapper behaviorMapper) {
        return new ProfileStage(behaviorMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FeedbackService feedbackService(BehaviorMapper behaviorMapper) {
        return new FeedbackService(behaviorMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(EmbeddingModel.class)
    public LongTermMemoryService longTermMemoryService(UserMemoryMapper userMemoryMapper,
                                                         EmbeddingModel embeddingModel) {
        return new LongTermMemoryService(userMemoryMapper, embeddingModel);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(ChatModel.class)
    public SummaryStage summaryStage(ChatModel chatModel, MemoryProperties properties) {
        return new SummaryStage(chatModel, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public MemoryPipeline memoryPipeline(List<MemoryStage> stages,
                                          HistoryStage historyStage) {
        return new MemoryPipeline(stages, historyStage);
    }
}
