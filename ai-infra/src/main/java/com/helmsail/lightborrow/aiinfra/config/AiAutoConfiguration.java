package com.helmsail.lightborrow.aiinfra.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helmsail.lightborrow.aiinfra.embedding.CachedEmbeddingModel;
import com.helmsail.lightborrow.aiinfra.embedding.EmbeddingModel;
import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.provider.OpenAiChatModel;
import com.helmsail.lightborrow.aiinfra.provider.OpenAiEmbeddingModel;
import com.helmsail.lightborrow.aiinfra.vector.PgVectorStore;
import com.helmsail.lightborrow.aiinfra.vector.VectorStore;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import javax.sql.DataSource;

/**
 * AI 基础设施自动配置。
 * <p>
 * 提供 ChatModel、EmbeddingModel、VectorStore 的默认 Bean 装配。
 * 各 Bean 均标注 @ConditionalOnMissingBean，允许应用层覆盖。
 * </p>
 */
@AutoConfiguration
@EnableConfigurationProperties(AiProperties.class)
public class AiAutoConfiguration {

    /**
     * LLM 聊天专用 RestClient。<p>
     * 不硬编码 baseUrl 和 apiKey，通过 {@link DynamicAuthInterceptor} 每次请求实时读取配置。
     */
    @Bean
    @ConditionalOnMissingBean(name = "llmRestClient")
    public RestClient llmRestClient(AiProperties properties) {
        AiProperties.LlmProperties llm = properties.getLlm();
        return RestClient.builder()
                .requestInterceptor(new DynamicAuthInterceptor(llm::getBaseUrl, llm::getApiKey))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * LLM 流式调用专用 WebClient。<p>
     * 不硬编码 baseUrl 和 apiKey，通过 {@link DynamicExchangeFilter} 每次请求实时读取配置。
     */
    @Bean
    @ConditionalOnMissingBean(name = "llmWebClient")
    public WebClient llmWebClient(AiProperties properties) {
        AiProperties.LlmProperties llm = properties.getLlm();
        return WebClient.builder()
                .filter(new DynamicExchangeFilter(llm::getBaseUrl, llm::getApiKey))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Embedding 专用 RestClient。<p>
     * 不硬编码 baseUrl 和 apiKey，通过 {@link DynamicAuthInterceptor} 每次请求实时读取配置。
     */
    @Bean
    @ConditionalOnMissingBean(name = "embeddingRestClient")
    public RestClient embeddingRestClient(AiProperties properties) {
        AiProperties.EmbeddingProperties emb = properties.getEmbedding();
        return RestClient.builder()
                .requestInterceptor(new DynamicAuthInterceptor(emb::getBaseUrl, emb::getApiKey))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(ChatModel.class)
    public OpenAiChatModel chatModel(RestClient llmRestClient,
                                     WebClient llmWebClient,
                                     AiProperties properties,
                                     ObjectMapper objectMapper,
                                     RetryRegistry aiRetryRegistry,
                                     CircuitBreakerRegistry aiCircuitBreakerRegistry) {
        return new OpenAiChatModel(
                llmRestClient, llmWebClient,
                properties.getLlm(), objectMapper,
                aiRetryRegistry, aiCircuitBreakerRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(EmbeddingModel.class)
    public OpenAiEmbeddingModel embeddingModel(RestClient embeddingRestClient,
                                               AiProperties properties,
                                               RetryRegistry aiRetryRegistry,
                                               CircuitBreakerRegistry aiCircuitBreakerRegistry) {
        return new OpenAiEmbeddingModel(
                embeddingRestClient, properties.getEmbedding(),
                aiRetryRegistry, aiCircuitBreakerRegistry);
    }

    /**
     * 带缓存的 Embedding 模型。当需要缓存时启用 {@code ai.embedding.cache.enabled=true}。
     */
    @Bean
    @ConditionalOnMissingBean(name = "cachedEmbeddingModel")
    @ConditionalOnProperty(prefix = "ai.embedding.cache", name = "enabled", havingValue = "true")
    public CachedEmbeddingModel cachedEmbeddingModel(EmbeddingModel embeddingModel) {
        return new CachedEmbeddingModel(embeddingModel, 1000);
    }

    /**
     * PgVectorStore 自动配置。仅在类路径存在 DataSource + JdbcTemplate 时生效。
     */
    @AutoConfiguration
    @ConditionalOnClass({DataSource.class, JdbcTemplate.class})
    @ConditionalOnProperty(prefix = "ai.vector", name = "enabled", havingValue = "true", matchIfMissing = false)
    public static class PgVectorStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(VectorStore.class)
        public PgVectorStore pgVectorStore(JdbcTemplate jdbcTemplate, AiProperties properties) {
            return new PgVectorStore(jdbcTemplate, properties.getVector());
        }
    }
}
