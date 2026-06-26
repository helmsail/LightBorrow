package com.helmsail.lightborrow.memory.config;

import com.helmsail.lightborrow.memory.model.MemoryContext;
import com.helmsail.lightborrow.memory.model.SessionState;
import com.helmsail.lightborrow.memory.pipeline.HistoryStage;
import com.helmsail.lightborrow.memory.pipeline.MemoryPipeline;
import com.helmsail.lightborrow.memory.pipeline.MemoryStage;
import com.helmsail.lightborrow.memory.pipeline.ProfileStage;
import com.helmsail.lightborrow.memory.pipeline.SessionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Memory 模块自动配置。
 *
 * <p>当 Redis 可用时，使用 Redis 背书的 SessionStage 和 HistoryStage；
 * 当 Redis 不可用时（如 dev-nodocker 模式），自动降级为
 * {@link ConcurrentHashMap} 内存存储，适合本地开发调试。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(MemoryProperties.class)
public class MemoryAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MemoryAutoConfiguration.class);

    // 内存存储（Redis 不可用时降级使用）
    private final Map<String, SessionState> sessionStore = new ConcurrentHashMap<>();
    private final Map<String, List<String>> historyStore = new ConcurrentHashMap<>();

    @Bean
    @ConditionalOnMissingBean
    public SessionStage sessionStage(ObjectProvider<StringRedisTemplate> templateProvider,
                                     MemoryProperties properties) {
        StringRedisTemplate template = templateProvider.getIfAvailable();
        if (template != null) {
            log.info("[Memory] Redis 可用，创建 Redis 版 SessionStage");
            return new SessionStage(template);
        }
        log.warn("[Memory] Redis 不可用，创建内存版 SessionStage（重启后数据丢失）");
        Map<String, SessionState> store = sessionStore;
        return new SessionStage(null) {
            @Override
            public void load(MemoryContext ctx) {
                String userId = ctx.getUserId();
                SessionState existing = store.get(userId);
                if (existing == null) {
                    ctx.setNewSession(true);
                    ctx.setSessionState(SessionState.builder()
                            .userId(userId)
                            .status("ACTIVE")
                            .createdAt(System.currentTimeMillis())
                            .lastAccessAt(System.currentTimeMillis())
                            .build());
                } else {
                    ctx.setNewSession(false);
                    ctx.setSessionState(existing);
                }
            }

            @Override
            public void save(MemoryContext ctx) {
                SessionState state = ctx.getSessionState();
                if (state == null) return;
                state.setLastAccessAt(System.currentTimeMillis());
                store.put(ctx.getUserId(), state);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public HistoryStage historyStage(ObjectProvider<StringRedisTemplate> templateProvider,
                                      MemoryProperties properties) {
        StringRedisTemplate template = templateProvider.getIfAvailable();
        int maxHistory = properties != null ? properties.getMaxHistory() : 20;
        if (template != null) {
            log.info("[Memory] Redis 可用，创建 Redis 版 HistoryStage, maxHistory={}",
                    properties.getMaxHistory());
            return new HistoryStage(template, properties);
        }
        log.warn("[Memory] Redis 不可用，创建内存版 HistoryStage, maxHistory={}", maxHistory);
        Map<String, List<String>> store = historyStore;
        return new HistoryStage(null, properties) {
            @Override
            public void load(MemoryContext ctx) {
                String userId = ctx.getUserId();
                List<String> messages = store.get(userId);
                if (messages == null) {
                    messages = new ArrayList<>();
                }
                int size = messages.size();
                List<String> recent = messages.subList(
                        Math.max(0, size - maxHistory), size);
                ctx.setHistoryMessages(new ArrayList<>(recent));
            }

            @Override
            public void save(MemoryContext ctx) {
                // 内存模式 save 无操作
            }

            @Override
            public void appendMessage(String userId, String messageJson) {
                store.computeIfAbsent(userId, k -> new ArrayList<>()).add(messageJson);
                List<String> messages = store.get(userId);
                if (messages.size() > maxHistory) {
                    List<String> trimmed = new ArrayList<>(
                            messages.subList(messages.size() - maxHistory, messages.size()));
                    store.put(userId, trimmed);
                }
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(JdbcTemplate.class)
    public ProfileStage profileStage(JdbcTemplate jdbcTemplate) {
        return new ProfileStage(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public MemoryPipeline memoryPipeline(List<MemoryStage> stages,
                                          HistoryStage historyStage) {
        return new MemoryPipeline(stages, historyStage);
    }
}
