package com.helmsail.lightborrow.memory.config;

import com.helmsail.lightborrow.memory.pipeline.HistoryStage;
import com.helmsail.lightborrow.memory.pipeline.MemoryPipeline;
import com.helmsail.lightborrow.memory.pipeline.MemoryStage;
import com.helmsail.lightborrow.memory.pipeline.ProfileStage;
import com.helmsail.lightborrow.memory.pipeline.SessionStage;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(MemoryProperties.class)
public class MemoryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(StringRedisTemplate.class)
    public SessionStage sessionStage(StringRedisTemplate stringRedisTemplate) {
        return new SessionStage(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(StringRedisTemplate.class)
    public HistoryStage historyStage(StringRedisTemplate stringRedisTemplate,
                                     MemoryProperties properties) {
        return new HistoryStage(stringRedisTemplate, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(JdbcTemplate.class)
    public ProfileStage profileStage(JdbcTemplate jdbcTemplate) {
        return new ProfileStage(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(HistoryStage.class)
    public MemoryPipeline memoryPipeline(List<MemoryStage> stages,
                                         HistoryStage historyStage) {
        return new MemoryPipeline(stages, historyStage);
    }
}
