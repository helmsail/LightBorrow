package com.helmsail.lightborrow.aiinfra.config;

import com.helmsail.lightborrow.aiinfra.embedding.CachedEmbeddingModel;
import com.helmsail.lightborrow.aiinfra.embedding.EmbeddingModel;
import com.helmsail.lightborrow.aiinfra.llm.ChatModel;
import com.helmsail.lightborrow.aiinfra.mapper.VectorDocumentMapper;
import com.helmsail.lightborrow.aiinfra.provider.OpenAiChatModel;
import com.helmsail.lightborrow.aiinfra.provider.OpenAiEmbeddingModel;
import com.helmsail.lightborrow.aiinfra.vector.PgVectorStore;
import com.helmsail.lightborrow.aiinfra.vector.VectorStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * 提供 ChatModel、EmbeddingModel、VectorStore 的默认 Bean 装配。
 * 各 Bean 均标注 @ConditionalOnMissingBean，允许应用层覆盖。
 */
@AutoConfiguration
@EnableConfigurationProperties(AiProperties.class)
public class AiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "llmRestClient")
    public RestClient llmRestClient(AiProperties properties) {
        AiProperties.LlmProperties llm = properties.getLlm();
        return RestClient.builder()
                .baseUrl(llm.getBaseUrl())
                .requestInterceptor(new DynamicAuthInterceptor(llm::getApiKey))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "embeddingRestClient")
    public RestClient embeddingRestClient(AiProperties properties) {
        AiProperties.EmbeddingProperties emb = properties.getEmbedding();
        return RestClient.builder()
                .baseUrl(emb.getBaseUrl())
                .requestInterceptor(new DynamicAuthInterceptor(emb::getApiKey))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(ChatModel.class)
    public OpenAiChatModel chatModel(RestClient llmRestClient,
                                     AiProperties properties) {
        return new OpenAiChatModel(
                llmRestClient, properties.getLlm());
    }

    @Bean
    @ConditionalOnMissingBean(EmbeddingModel.class)
    public OpenAiEmbeddingModel embeddingModel(RestClient embeddingRestClient,
                                               AiProperties properties) {
        return new OpenAiEmbeddingModel(
                embeddingRestClient, properties.getEmbedding());
    }

    @Bean
    @ConditionalOnMissingBean(name = "cachedEmbeddingModel")
    @ConditionalOnProperty(prefix = "ai.embedding.cache", name = "enabled", havingValue = "true")
    public CachedEmbeddingModel cachedEmbeddingModel(EmbeddingModel embeddingModel) {
        return new CachedEmbeddingModel(embeddingModel, 1000);
    }

    @AutoConfiguration
    @ConditionalOnClass(VectorDocumentMapper.class)
    @ConditionalOnProperty(prefix = "ai.vector", name = "enabled", havingValue = "true", matchIfMissing = false)
    public static class PgVectorStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(VectorStore.class)
        public PgVectorStore pgVectorStore(VectorDocumentMapper mapper, AiProperties properties) {
            return new PgVectorStore(mapper, properties.getVector());
        }
    }
}
